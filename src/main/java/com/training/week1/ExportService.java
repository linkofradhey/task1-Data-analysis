package com.training.week1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.training.week1.DataSummary.NumericSummary;

@Service
public class ExportService {

	@Autowired
	private DataAnalysisService dataService;

	@Autowired
	private ChartService chartService;

	// ── Both services injected via constructor ──
	public ExportService(DataAnalysisService dataService, ChartService chartService) {
		this.dataService = dataService;
		this.chartService = chartService;
	}

	// ═══════════════════════════════════════════════════════════
	// MAIN METHOD — Generates a complete .xlsx report
	// Called by: ExportController
	// Returns: byte[] (the entire Excel file in memory)
	// ═══════════════════════════════════════════════════════════
	public byte[] generateExcelReport() throws IOException {

		// 1. Create a new Excel Workbook (this is the .xlsx file)
		XSSFWorkbook workbook = new XSSFWorkbook();

		// 2. Create reusable styles (header bold + colored, data normal)
		CellStyle headerStyle = createHeaderStyle(workbook);
		CellStyle dataStyle = createDataStyle(workbook);

		// 3. Build each sheet by calling your EXISTING service methods
		buildSummarySheet(workbook, headerStyle, dataStyle);
		buildMissingValuesSheet(workbook, headerStyle, dataStyle);
		buildDataTypesSheet(workbook, headerStyle, dataStyle);
		buildRawDataSheet(workbook, headerStyle, dataStyle);
		buildChartsSheet(workbook);

		// 4. Convert workbook to byte array and return
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		return outputStream.toByteArray();
	}

	// ═══════════════════════════════════════════════════════════════════
//  HELPER: Embed a PNG image into an Excel sheet
//
//  HOW IT WORKS:
//    1. addPicture()   → registers the image bytes in the workbook
//                        and returns a pictureIndex
//    2. createDrawingPatriarch() → gets the sheet's drawing canvas
//    3. XSSFClientAnchor → defines WHERE the image is placed
//         - col1, row1 = top-left corner of the image
//         - col2, row2 = bottom-right corner of the image
//    4. createPicture() → draws the image on the canvas
//
//  PARAMETERS:
//    - imageBytes: PNG image as byte array (from ChartService)
//    - startRow:   row index for top-left corner
//    - startCol:   column index for top-left corner
// ═══════════════════════════════════════════════════════════════════
	private void embedImage(XSSFWorkbook workbook, XSSFSheet sheet, byte[] imageBytes, int startRow, int startCol) {

		// ── Step 1: Register image in workbook ──
		int pictureIndex = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG // image format
		);

		// ── Step 2: Get drawing canvas for this sheet ──
		XSSFDrawing drawing = sheet.createDrawingPatriarch();

		// ── Step 3: Define image position and size ──
		// XSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2)
		// dx/dy = offset in EMUs (0 = no offset from cell edge)
		// col1/row1 = top-left cell
		// col2/row2 = bottom-right cell (controls image size)
		XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, // no pixel offsets
				startCol, // top-left column
				startRow, // top-left row
				startCol + 9, // bottom-right column (9 cols wide)
				startRow + 20 // bottom-right row (20 rows tall)
		);

		// ── Step 4: Draw the image on the sheet ──
		drawing.createPicture(anchor, pictureIndex);
	}

	// ═══════════════════════════════════════════════════════════
//  SHEET: Summary Statistics
//
//  Source  : dataService.getDataSummary() → List<DataSummary>
//  Numeric columns  → mean, std, min, max, median populated
//  Categorical cols → unique, mostFrequent, frequency populated
// ═══════════════════════════════════════════════════════════
	private void buildSummarySheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle) {

		XSSFSheet sheet = workbook.createSheet("Summary Statistics");

		// ── Step 1: Define column headers ──
		String[] headers = { "Column", "Type", "Count", "Mean", "Std Dev", "Min", "Max", "Median", // Numeric fields
				"Unique", "Most Frequent", "Frequency" // Categorical fields
		};
		writeHeaderRow(sheet, headers, headerStyle);

		// ── Step 2: Fetch data from service ──
		List<DataSummary.ColumnResult> summaries = dataService.getDataSummary();

		// ── Step 3: Write each row ──
		for (int i = 0; i < summaries.size(); i++) {
			DataSummary.ColumnResult s = summaries.get(i);
			Row row = sheet.createRow(i + 1); // row 0 is header

			// ── Common columns (always present) ──
			setCell(row, 0, s.column, dataStyle);
			setCell(row, 1, s.type, dataStyle);

			if ("numeric".equalsIgnoreCase(s.type) && s.numeric != null) {

				// ── Numeric branch ──
				NumericSummary n = s.numeric;

				setNumCell(row, 2, (double) n.count, dataStyle);
				setNumCell(row, 3, n.mean, dataStyle);
				setNumCell(row, 4, n.std, dataStyle);
				setNumCell(row, 5, n.min, dataStyle);
				setNumCell(row, 6, n.max, dataStyle);
				setNumCell(row, 7, n.median, dataStyle);

				// Categorical columns → placeholder dashes
				setCell(row, 8, "-", dataStyle);
				setCell(row, 9, "-", dataStyle);
				setCell(row, 10, "-", dataStyle);

			} else if ("categorical".equalsIgnoreCase(s.type) && s.categorical != null) {

				// ── Categorical branch ──
				DataSummary.CategoricalSummary c = s.categorical;

				setNumCell(row, 2, (double) c.count, dataStyle);

				// Numeric columns → placeholder dashes
				setCell(row, 3, "-", dataStyle);
				setCell(row, 4, "-", dataStyle);
				setCell(row, 5, "-", dataStyle);
				setCell(row, 6, "-", dataStyle);
				setCell(row, 7, "-", dataStyle);

				setNumCell(row, 8, (double) c.uniqueValues, dataStyle);
				setCell(row, 9, c.mostFrequent, dataStyle); // null-safe via setCell
				setNumCell(row, 10, (double) c.mostFrequentCount, dataStyle);

			} else {

				// ── Fallback: unknown type or null sub-object ──
				for (int col = 2; col <= 10; col++) {
					setCell(row, col, "N/A", dataStyle);
				}
			}
		}

		// ── Step 4: Auto-size all columns for clean display ──
		autoSize(sheet, headers.length);
	}

	// ═══════════════════════════════════════════════════════════
//  HELPER: Write a styled header row at row index 0
//
//  WHY XSSFSheet instead of Sheet:
//    - We're using XSSFWorkbook (xlsx format) throughout
//    - XSSFSheet gives access to autoSizeColumn() directly
//
//  WHY row index 0:
//    - Header always occupies the first row
//    - Data rows start from createRow(i + 1) in sheet builders
// ═══════════════════════════════════════════════════════════
	private void writeHeaderRow(XSSFSheet sheet, String[] headers, CellStyle style) {

		// Step 1: Create the first row (index 0) as the header row
		Row headerRow = sheet.createRow(0);

		// Step 2: Iterate over each header label and write it as a cell
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style); // Apply bold/colored header style
		}
	}

	// ═══════════════════════════════════════════════════════════
//  HELPER: Write a Numeric cell
//
//  WHY Double (boxed) instead of double (primitive):
//    - Allows null check — DTO fields like mean, std, median
//      are null for categorical columns
//    - NaN / Infinity checks prevent Excel file corruption
//
//  WHY cell.setCellValue(double) instead of String:
//    - Excel treats it as a real number → sortable, filterable,
//      usable in SUM/AVERAGE formulas
// ═══════════════════════════════════════════════════════════
	private void setNumCell(Row row, int col, Double value, CellStyle style) {

		// Step 1: Create the cell at the given column index
		Cell cell = row.createCell(col);

		// Step 2: Guard against invalid values
		if (value == null || value.isNaN() || value.isInfinite()) {
			// Write safe text fallback — avoids Excel corruption
			cell.setCellValue("-");
		} else {
			// Write as a true numeric cell (double overload)
			cell.setCellValue(value);
		}

		// Step 3: Apply style
		cell.setCellStyle(style);
	}

	// ═══════════════════════════════════════════════════════════
	// SHEET 2: Missing Values
	// ═══════════════════════════════════════════════════════════
	private void buildMissingValuesSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle) {

		XSSFSheet sheet = workbook.createSheet("Missing Values");

		// Header row
		String[] headers = { "Column", "Missing Count" };
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		// Data rows — from YOUR EXISTING service
		List<AnalysisDtos.MissingDto> missingValues = dataService.getMissing();

		for (int rowIdx = 0; rowIdx < missingValues.size(); rowIdx++) {
			AnalysisDtos.MissingDto mv = missingValues.get(rowIdx);
			Row row = sheet.createRow(rowIdx + 1);

			setCell(row, 0, mv.getColumn(), dataStyle);
			setCell(row, 1, String.valueOf(mv.getMissingCount()), dataStyle);
		}

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
	}

	// ═══════════════════════════════════════════════════════════
	// SHEET 3: Inferred Data Types
	// ═══════════════════════════════════════════════════════════
	private void buildDataTypesSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle) {

		XSSFSheet sheet = workbook.createSheet("Data Types");

		String[] headers = { "Column", "Inferred Type" };
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		List<AnalysisDtos.DtypeDto> dtypes = dataService.getDtypes();

		for (int rowIdx = 0; rowIdx < dtypes.size(); rowIdx++) {
			AnalysisDtos.DtypeDto dt = dtypes.get(rowIdx);
			Row row = sheet.createRow(rowIdx + 1);

			setCell(row, 0, dt.getColumn(), dataStyle);
			setCell(row, 1, dt.getDtype(), dataStyle);
		}

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
	}

	// ═══════════════════════════════════════════════════════════
	// SHEET 4: Raw CSV Data
	// ═══════════════════════════════════════════════════════════
	private void buildRawDataSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle) {

		XSSFSheet sheet = workbook.createSheet("Raw Data");

		// Get headers and rows from YOUR EXISTING service
//		List<String>csvHeaders = dataService.getHeaders();
		// ── Step 1: Get column headers from service (not CsvLoader) ──
		List<String> headers = dataService.getHeaders();

		if (headers.isEmpty()) {
			sheet.createRow(0).createCell(0).setCellValue("No data available");
			return;
		}

		// ── Step 2: Write header row ──
		writeHeaderRow(sheet, headers.toArray(new String[0]), headerStyle);

		// ── Step 3: Get all raw rows ──
		List<Map<String, String>> rows = dataService.getRaw();

		// ── Step 4: Write each data row ──
		for (int i = 0; i < rows.size(); i++) {
			Map<String, String> rowData = rows.get(i);
			Row excelRow = sheet.createRow(i + 1); // i+1 because row 0 is header

			for (int j = 0; j < headers.size(); j++) {
				String colName = headers.get(j);
				String value = rowData.get(colName);

				Cell cell = excelRow.createCell(j);
				cell.setCellStyle(dataStyle);

				// ── Try writing as number, fall back to String ──
				if (value != null && !value.trim().isEmpty()) {
					try {
						cell.setCellValue(Double.parseDouble(value.trim()));
					} catch (NumberFormatException e) {
						cell.setCellValue(value); // plain text
					}
				} else {
					cell.setCellValue(""); // empty cell for nulls
				}
			}
		}

		// ── Step 5: Auto-fit all columns ──
		autoSize(sheet, headers.size());
	}

	// ═══════════════════════════════════════════════════════════════════
//  CHARTS SHEET
//
//  WHAT IT DOES:
//    1. Calls ChartService to generate chart images as byte[]
//    2. Adds each image to the workbook's image registry
//    3. Creates an anchor to position the image on the sheet
//    4. Draws the image using XSSFDrawing
//
//  KEY FIX:
//    - generateBarChart() now takes List<MissingDto>, NOT raw Map
//    - generatePieChart() now takes List<Map<String,String>> + column name
//    - Both return byte[] (PNG image bytes)
// ═══════════════════════════════════════════════════════════════════
	private void buildChartsSheet(XSSFWorkbook workbook) {
		try {
			XSSFSheet sheet = workbook.createSheet("Charts");

			// ══════════════════════════════════════════════
			// CHART 1: Bar Chart — Missing Values
			// ══════════════════════════════════════════════

			// ── Fetch missing data using the CORRECT return type ──
			List<AnalysisDtos.MissingDto> missingData = dataService.getMissing();

			// ── Generate bar chart — passes List<MissingDto> directly ──
			byte[] barChartBytes;

			barChartBytes = chartService.generateBarChart(missingData, "Missing Values by Column");

			// ── Embed bar chart at row 0, col 0 ──
			if (barChartBytes != null && barChartBytes.length > 0) {
				embedImage(workbook, sheet, barChartBytes, 0, 0);
			}

			// ══════════════════════════════════════════════
			// CHART 2: Pie Chart — Category Distribution
			// ══════════════════════════════════════════════

			// ── Fetch raw data — returns List<Map<String, String>> ──
			List<Map<String, String>> rawData = dataService.getRaw();

			// ── Define which column to group by for the pie chart ──
			String categoryColumn = "Department"; // adjust to your actual column name

			// ── Generate pie chart — passes raw data + column name ──
			byte[] pieChartBytes;
			pieChartBytes = chartService.generatePieChart(rawData, categoryColumn, "Distribution by " + categoryColumn);
			// TODO Auto-generated catch block

			// ── Embed pie chart at row 0, col 10 (next to bar chart) ──
			if (pieChartBytes != null && pieChartBytes.length > 0) {
				embedImage(workbook, sheet, pieChartBytes, 0, 10);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
	}

	// ═══════════════════════════════════════════════════════════
	// HELPER: Embed a PNG image into an Excel sheet
	// ═══════════════════════════════════════════════════════════
	//
	// HOW IT WORKS:
	// 1. Add the image bytes to the workbook (gets an index)
	// 2. Create a "drawing canvas" on the sheet
	// 3. Create an "anchor" that defines WHERE the image sits
	// 4. Insert the picture at that anchor position
	//
	private void embedImage(XSSFWorkbook workbook, XSSFSheet sheet, byte[] imageBytes, int startRow, int startCol,
			int endRow, int endCol) {

		// Step 1: Add image to workbook's image collection
		int pictureIndex = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

		// Step 2: Get or create the drawing canvas for this sheet
		XSSFDrawing drawing = sheet.createDrawingPatriarch();

		// Step 3: Define the anchor (position + size)
		// Parameters: dx1, dy1, dx2, dy2, col1, row1, col2, row2
		// dx/dy are offsets within the cell (0 = top-left corner)
		XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, startCol, startRow, endCol, endRow);
		anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

		// Step 4: Insert the picture
		drawing.createPicture(anchor, pictureIndex);
	}

	private void setCell(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value != null ? value : "-");
		cell.setCellStyle(style);
	}

	// ═══════════════════════════════════════════════════════════
	// HELPER: Auto-size all columns in a sheet
	// ═══════════════════════════════════════════════════════════
	private void autoSize(XSSFSheet sheet, int colCount) {
		for (int i = 0; i < colCount; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	// ═══════════════════════════════════════════════════════════
	// HELPER: Header cell style
	// → Bold white text on dark blue background
	// ═══════════════════════════════════════════════════════════
	private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
		XSSFCellStyle style = workbook.createCellStyle();

		// Background color — dark blue
		style.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 31, (byte) 78, (byte) 121 }, null));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// Font — bold, white, size 11
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null));
		font.setFontHeightInPoints((short) 11);
		style.setFont(font);

		// Borders
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);

		// Center-align text
		style.setAlignment(HorizontalAlignment.CENTER);

		return style;
	}

	// ═══════════════════════════════════════════════════════════
	// HELPER: Data cell style
	// → Normal text, light grey background, with borders
	// ═══════════════════════════════════════════════════════════
	private CellStyle createDataStyle(XSSFWorkbook workbook) {
		XSSFCellStyle style = workbook.createCellStyle();

		// Light grey background for readability
		style.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 242, (byte) 242, (byte) 242 }, null));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// Normal font
		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		style.setFont(font);

		// Borders
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);

		return style;
	}
	
	// Inside ExportService.java
	public byte[] generateZipReport() throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    try (ZipOutputStream zos = new ZipOutputStream(baos)) {

	        // ── Add Excel file into ZIP ──
	        byte[] excelBytes = generateExcelReport();
	        ZipEntry excelEntry = new ZipEntry("dashboard_report.xlsx");
	        zos.putNextEntry(excelEntry);
	        zos.write(excelBytes);
	        zos.closeEntry();

	        // ── Optionally add bar chart PNG ──
	        byte[] barChart = chartService.generateBarChart(
	            dataService.getMissing(), "Missing Values"
	        );
	        ZipEntry barEntry = new ZipEntry("charts/bar_chart.png");
	        zos.putNextEntry(barEntry);
	        zos.write(barChart);
	        zos.closeEntry();

	        // ── Optionally add pie chart PNG ──
	        byte[] pieChart = chartService.generatePieChart(
	            dataService.getRaw(), "Department", "Department Distribution"
	        );
	        ZipEntry pieEntry = new ZipEntry("charts/pie_chart.png");
	        zos.putNextEntry(pieEntry);
	        zos.write(pieChart);
	        zos.closeEntry();
	    }

	    return baos.toByteArray();
	}
}