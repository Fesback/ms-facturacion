package com.fescode.service.impl;


import com.fescode.dto.response.PedidoPdfDTO;
import com.fescode.service.PdfService;

import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
import java.text.DecimalFormat;

import static java.io.File.separator;


@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(50, 50, 50));
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(70, 130, 180));
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font TOTAL_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(255, 69, 0));


    @Override
    public byte[] generarBoletaDesdeDto(PedidoPdfDTO dto) throws Exception {
        validarPedido(dto);

         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new PdfPageEventHelper() {});

            document.open();
            agregarHeader(document, dto);
            agregarCustomerInfo(document, dto);
            agregarItemsTable(document, dto.getItems());
            agregarTotales(document, dto.getTotal());
            agregarFooter(document);
            document.close();

            validarPdfGenerado(baos);
            return baos.toByteArray();
        }
    }

    private void validarPedido(PedidoPdfDTO dto) {
        if (dto == null) throw new IllegalArgumentException("El pedido no puede ser nulo");
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalStateException("El pedido no contiene ítems");
        }
    }

    private void agregarHeader(Document document, PedidoPdfDTO dto) throws Exception {
        float marginRight = 40f;
        float logoSize = 150f;

        try {
            URL logoUrl = getClass().getResource("/images/NovaShopLogo.png");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(logoSize, logoSize);

                float logoX = document.getPageSize().getWidth() - logo.getScaledWidth() - marginRight;
                float logoY = document.getPageSize().getHeight() - 200f;
                logo.setAbsolutePosition(logoX, logoY);
                document.add(logo);
            }
        } catch (Exception e) {
            System.err.println("Error logo: " + e.getMessage());
        }

        Paragraph headerText = new Paragraph();
        headerText.add(new Chunk("NOVASHOP\n", HEADER_FONT));
        headerText.add(new Chunk("COMPROBANTE DE PEDIDO\n\n", TITLE_FONT));
        headerText.setAlignment(Element.ALIGN_LEFT);
        document.add(headerText);

        Paragraph infoPedido = new Paragraph();
        infoPedido.add(new Chunk("Pedido n.º: ", BOLD_FONT));
        infoPedido.add(new Chunk(dto.getIdPedido().toString() + "\n", NORMAL_FONT));
        infoPedido.add(new Chunk("Fecha: ", BOLD_FONT));
        infoPedido.add(new Chunk(dto.getFechaPedido(), NORMAL_FONT));
        infoPedido.setSpacingBefore(10f);
        document.add(infoPedido);
    }

    private void agregarCustomerInfo(Document document, PedidoPdfDTO dto) throws DocumentException {
        document.add(new Paragraph("\n\n\n\n\n\n\n\n"));

        PdfPTable container = new PdfPTable(1);
        container.setWidthPercentage(60);
        container.setHorizontalAlignment(Element.ALIGN_LEFT);
        container.setSpacingBefore(0f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8f);

        Paragraph customerInfo = new Paragraph();
        customerInfo.add(new Chunk("Cliente: ", BOLD_FONT));
        customerInfo.add(new Chunk(dto.getNombreCliente().toUpperCase(), NORMAL_FONT));
        customerInfo.add(Chunk.NEWLINE);
        customerInfo.add(new Chunk("Dirección: ", BOLD_FONT));
        customerInfo.add(new Chunk(dto.getDireccionEnvio(), NORMAL_FONT));
        customerInfo.add(Chunk.NEWLINE);
        customerInfo.add(new Chunk("Teléfono: ", BOLD_FONT));
        customerInfo.add(new Chunk(dto.getEmailCliente(), NORMAL_FONT)); // Aquí usamos email como contacto

        cell.addElement(customerInfo);
        container.addCell(cell);
        document.add(container);

        document.add(new Paragraph("\n"));
    }

    private void agregarItemsTable(Document document, List<PedidoPdfDTO.ItemPedidoDTO> items) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50f, 15f, 20f, 25f});
        table.setSpacingBefore(15f);
        table.setSpacingAfter(20f);

        BaseColor headerBgColor = new BaseColor(70, 130, 180);
        agregarCeldaEncabezado(table, "Artículo", headerBgColor, Element.ALIGN_LEFT);
        agregarCeldaEncabezado(table, "Cantidad", headerBgColor, Element.ALIGN_CENTER);
        agregarCeldaEncabezado(table, "Precio Unitario", headerBgColor, Element.ALIGN_RIGHT);
        agregarCeldaEncabezado(table, "Subtotal", headerBgColor, Element.ALIGN_RIGHT);

        boolean isEvenRow = false;
        BaseColor rowEvenColor = new BaseColor(255, 255, 255);
        BaseColor rowOddColor = new BaseColor(240, 240, 240);
        BaseColor borderColor = new BaseColor(200, 200, 200);

        for (PedidoPdfDTO.ItemPedidoDTO item : items) {
            BaseColor rowColor = isEvenRow ? rowEvenColor : rowOddColor;
            double subtotal = item.getCantidad() * item.getPrecio();

            agregarCeldaContenido(table, item.getNombreProducto(), rowColor, borderColor, Element.ALIGN_LEFT);
            agregarCeldaContenido(table, String.valueOf(item.getCantidad()), rowColor, borderColor, Element.ALIGN_CENTER);
            agregarCeldaContenido(table, formatCurrency(item.getPrecio()), rowColor, borderColor, Element.ALIGN_RIGHT);
            agregarCeldaContenido(table, formatCurrency(subtotal), rowColor, borderColor, Element.ALIGN_RIGHT);

            isEvenRow = !isEvenRow;
        }
        document.add(table);
    }

    private void agregarTotales(Document document, Double total) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(15f);

        PdfPCell separator = new PdfPCell(new Phrase(" "));
        separator.setBorder(Rectangle.TOP);
        separator.setFixedHeight(10f);
        separator.setColspan(2);
        table.addCell(separator);

        agregarCeldaTotal(table, "Subtotal:", true);
        agregarCeldaTotal(table, formatCurrency(total), false);
        agregarCeldaTotal(table, "Impuestos (0%):", true);
        agregarCeldaTotal(table, formatCurrency(0.0), false);

        separator = new PdfPCell(new Phrase(" "));
        separator.setBorder(Rectangle.TOP);
        separator.setFixedHeight(2f);
        separator.setColspan(2);
        table.addCell(separator);

        agregarCeldaTotal(table, "Total:", true);
        agregarCeldaTotal(table, formatCurrency(total), true);

        document.add(table);
    }

    private void agregarFooter(Document document) throws DocumentException {
        for (int i = 0; i < 10; i++) {
            document.add(new Paragraph(" "));
        }

        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Phrase(" "));
        lineCell.setBorder(Rectangle.TOP);
        lineCell.setFixedHeight(1f);
        separator.addCell(lineCell);
        document.add(separator);

        Paragraph footer = new Paragraph();
        footer.add(new Chunk("¡Gracias por su compra!", BOLD_FONT));
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("NOVASHOP - Av. Camino Real 1281, Lima, Perú", NORMAL_FONT));
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("Teléfono: (01) 987654321 - Email: contacto@novashop.com", NORMAL_FONT));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(5f);
        document.add(footer);
    }

    private void agregarCeldaEncabezado(PdfPTable table, String texto, BaseColor bgColor, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(8);
        cell.setBorderWidth(1f);
        cell.setBorderColor(bgColor);
        table.addCell(cell);
    }

    private void agregarCeldaContenido(PdfPTable table, String texto, BaseColor bgColor, BaseColor borderColor, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, NORMAL_FONT));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(6);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(borderColor);
        table.addCell(cell);
    }

    private void agregarCeldaTotal(PdfPTable table, String texto, boolean isBold) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, isBold ? BOLD_FONT : NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String formatCurrency(Double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "S/. " + (amount != null ? df.format(amount) : "0.00");
    }

    private void validarPdfGenerado(ByteArrayOutputStream baos) {
        if (baos.size() == 0) throw new IllegalStateException("El PDF generado está vacío");
    }
}

