package br.com.printpilot.service;

import br.com.printpilot.entity.Customer;
import br.com.printpilot.entity.Quote;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class QuotePdfService {

    private final QuoteRepository quoteRepository;

    private static final float MARGIN = 50;
    private static final float START_Y = 800;
    private static final float LINE_HEIGHT = 15;

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado: id=" + quoteId));

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage();
            document.addPage(page);

            PdfContext ctx = new PdfContext(document, page);

            writeHeader(ctx);
            writeQuoteInfo(ctx, quote);
            writeCustomerInfo(ctx, quote.getCustomer());
            writeProductInfo(ctx, quote);
            writeFinancials(ctx, quote);
            writeFooter(ctx);

            ctx.contentStream.close();

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    private void writeHeader(PdfContext ctx) throws IOException {
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_BOLD, 18);
        ctx.writeLine("PRINTPILOT");
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_BOLD, 16);
        ctx.writeLine("ORÇAMENTO");
        ctx.newLine();
    }

    private void writeQuoteInfo(PdfContext ctx, Quote quote) throws IOException {
        ctx.setFont(Standard14Fonts.FontName.HELVETICA, 12);
        ctx.writeLine("Orçamento #" + quote.getId());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ctx.writeLine("Data: " + quote.getCreatedAt().format(formatter));
        
        String statusTranslated = translateStatus(quote.getStatus().name());
        ctx.writeLine("Status: " + statusTranslated);
        ctx.writeLine("Tipo: " + quote.getPricingType().name());
        ctx.newLine();
    }

    private void writeCustomerInfo(PdfContext ctx, Customer customer) throws IOException {
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_BOLD, 12);
        ctx.writeLine("Cliente");
        ctx.setFont(Standard14Fonts.FontName.HELVETICA, 12);
        
        if (customer == null) {
            ctx.writeLine("Nome: Não informado");
        } else {
            ctx.writeLine("Nome: " + customer.getName());
            if (customer.getDocument() != null && !customer.getDocument().isBlank()) {
                ctx.writeLine("Documento: " + customer.getDocument());
            }
            if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
                ctx.writeLine("E-mail: " + customer.getEmail());
            }
            if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
                ctx.writeLine("Telefone: " + customer.getPhone());
            }
        }
        ctx.newLine();
    }

    private void writeProductInfo(PdfContext ctx, Quote quote) throws IOException {
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_BOLD, 12);
        ctx.writeLine("Produto");
        ctx.setFont(Standard14Fonts.FontName.HELVETICA, 12);
        
        ctx.writeLine("Produto: " + quote.getProductName());
        ctx.writeLine("Material: " + quote.getMaterialName());
        ctx.writeLine("Quantidade: " + formatQuantity(quote.getQuantity()));

        if (quote.getPricingType() == PricingType.AREA) {
            if (quote.getWidth() != null && quote.getHeight() != null) {
                ctx.writeLine(String.format("Dimensões: %s m × %s m", formatDecimalNumber(quote.getWidth()), formatDecimalNumber(quote.getHeight())));
            }
            if (quote.getUnitArea() != null) {
                ctx.writeLine(String.format("Área unitária: %s m²", formatDecimalNumber(quote.getUnitArea())));
            }
            if (quote.getTotalArea() != null) {
                ctx.writeLine(String.format("Área total: %s m²", formatDecimalNumber(quote.getTotalArea())));
            }
        } else if (quote.getPricingType() == PricingType.QUANTITY) {
            if (quote.getUnitsPerSheet() != null) {
                ctx.writeLine("Aproveitamento: " + quote.getUnitsPerSheet() + " unidades/folha");
            }
            if (quote.getRequiredSheets() != null) {
                ctx.writeLine("Folhas necessárias: " + quote.getRequiredSheets());
            }
        }
        ctx.newLine();
    }

    private void writeFinancials(PdfContext ctx, Quote quote) throws IOException {
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_BOLD, 12);
        ctx.writeLine("Composição do orçamento");
        ctx.setFont(Standard14Fonts.FontName.HELVETICA, 12);

        ctx.writeAligned("Material", formatCurrency(quote.getMaterialCost()));
        ctx.writeAligned("Impressão", formatCurrency(quote.getPrintingCost()));
        ctx.writeAligned("Acabamentos", formatCurrency(quote.getFinishingCost()));
        ctx.writeAligned("Desperdício", formatCurrency(quote.getWasteCost()));
        ctx.writeAligned("Mão de obra", formatCurrency(quote.getLaborCost()));
        ctx.writeLine("--------------------------------------------------");
        ctx.writeAligned("Custo total", formatCurrency(quote.getTotalCost()));
        ctx.newLine();

        ctx.writeLine("Preço sugerido: " + formatCurrency(quote.getSuggestedPrice()));
        
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_BOLD, 14);
        ctx.writeLine("Preço final: " + formatCurrency(quote.getFinalPrice()));
        ctx.newLine();

        ctx.setFont(Standard14Fonts.FontName.HELVETICA, 12);
        ctx.writeLine("Margem configurada: " + formatDecimalNumber(quote.getMarginPercentage()) + "%");
        ctx.newLine();
    }

    private void writeFooter(PdfContext ctx) throws IOException {
        ctx.ensureSpace(40);
        ctx.setFont(Standard14Fonts.FontName.HELVETICA_OBLIQUE, 10);
        ctx.writeLine("Orçamento gerado pelo PrintPilot AI.");
        ctx.writeLine("Validade e condições comerciais devem ser confirmadas pela gráfica.");
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "DRAFT" -> "Rascunho";
            case "SENT" -> "Enviado";
            case "APPROVED" -> "Aprovado";
            case "REJECTED" -> "Rejeitado";
            case "EXPIRED" -> "Expirado";
            default -> status;
        };
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        DecimalFormat format = new DecimalFormat("R$ #,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return format.format(value);
    }

    private String formatDecimalNumber(BigDecimal value) {
        if (value == null) return "0,00";
        DecimalFormat format = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return format.format(value);
    }

    private String formatQuantity(Integer value) {
        if (value == null) return "0";
        DecimalFormat format = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return format.format(value);
    }

    private class PdfContext {
        PDDocument document;
        PDPage currentPage;
        PDPageContentStream contentStream;
        float currentY;
        Standard14Fonts.FontName currentFont;
        float currentFontSize;

        PdfContext(PDDocument document, PDPage page) throws IOException {
            this.document = document;
            this.currentPage = page;
            this.contentStream = new PDPageContentStream(document, page);
            this.currentY = START_Y;
        }

        void setFont(Standard14Fonts.FontName fontName, float size) throws IOException {
            this.currentFont = fontName;
            this.currentFontSize = size;
            contentStream.setFont(new PDType1Font(fontName), size);
        }

        void newLine() throws IOException {
            ensureSpace(LINE_HEIGHT);
            currentY -= LINE_HEIGHT;
        }

        void writeLine(String text) throws IOException {
            ensureSpace(LINE_HEIGHT);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, currentY);
            // Evitar problemas com caracteres especiais trocando por algo similar ou simplesmente confiando na fonte
            // Standard14Fonts não tem todos os caracteres UTF-8, mas tem o básico.
            // Para simplificar, vou remover caracteres problemáticos caso ocorram.
            contentStream.showText(sanitize(text));
            contentStream.endText();
            currentY -= LINE_HEIGHT;
        }

        void writeAligned(String label, String value) throws IOException {
            ensureSpace(LINE_HEIGHT);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, currentY);
            contentStream.showText(sanitize(label));
            
            // Simulação simples de alinhamento à direita para o valor
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText(sanitize(value));
            
            contentStream.endText();
            currentY -= LINE_HEIGHT;
        }

        void ensureSpace(float requiredSpace) throws IOException {
            if (currentY - requiredSpace < MARGIN) {
                contentStream.close();
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                if (currentFont != null) {
                    contentStream.setFont(new PDType1Font(currentFont), currentFontSize);
                }
                currentY = START_Y;
            }
        }

        String sanitize(String text) {
            if (text == null) return "";
            // Replace any char not in WinAnsiEncoding to prevent exception
            return text.replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]", "");
        }
    }
}
