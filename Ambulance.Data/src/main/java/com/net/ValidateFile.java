package com.net;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.poi.hpsf.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
public class ValidateFile {

	private static int outputRowNum = 0;
	static String outputPath = "/Users/jayk/Desktop/ValidatedFile.xlsx";
	static XSSFWorkbook outputWorkbook = new XSSFWorkbook();
	static XSSFSheet outputSheet = outputWorkbook.createSheet("ValidatedDataReport");



	@PostMapping("/processingFile")
	public ResponseEntity<String>  validateFile(@RequestParam("file") MultipartFile file, @RequestParam("dropdown") String selectedValue) {

		//modify to switch case - reminder
		if(selectedValue.equalsIgnoreCase("AMBULANCE")) {
			return Ambulance(file, selectedValue);
		}else if(selectedValue.equalsIgnoreCase("DRUGSERVICES")) {
			return DrugService(file,selectedValue);
		}
		return ResponseEntity.internalServerError().body("Error : " + "None of the file matched ");



	}

	public ResponseEntity<String> Ambulance(MultipartFile file, String selectedValue){


		try{

			XSSFSheet sheet = getInputSheetBasedOntype(file,selectedValue);
			int rowIndexTOStart = getRowIndexNo(sheet);
			XSSFSheet outputSheet = getExcelSheet();

			for (Row row : sheet) {
				if(row.getRowNum() < rowIndexTOStart) {
					continue;
				}

				Cell rateType = row.getCell(6);

				if(rateType != null && rateType.getCellType() == CellType.STRING && 
						rateType.getStringCellValue().trim().equalsIgnoreCase("DEF")) {

					Row outputRow = outputSheet.createRow(outputRowNum++);
					outputRow.createCell(0).setCellValue(row.getCell(0).toString()); 
					outputRow.createCell(1).setCellValue(row.getCell(2).toString());   
					outputRow.createCell(2).setCellValue(row.getCell(3).toString());   
					outputRow.createCell(3).setCellValue(row.getCell(4).toString());   
					outputRow.createCell(4).setCellValue(row.getCell(5).toString());
					outputRow.createCell(5).setCellValue(row.getCell(10).toString()); 
					outputRow.createCell(6).setCellValue(row.getCell(11).toString());
					outputRow.createCell(7).setCellValue(String.format("%.4f",row.getCell(13).getNumericCellValue()));;

				}

			} 

			//another sheet processing
			getPricingIndicator(file);

			try (FileOutputStream fos = new FileOutputStream(outputPath)) {
				outputWorkbook.write(fos);
				System.out.println("Filtered data written to: " + outputPath);
			}
			return ResponseEntity.ok("Report processed successfully!");

		}
		catch (IOException e) {
			return ResponseEntity.internalServerError().body("Error : " + e.getMessage());

		}
	}

	public ResponseEntity<String> DrugService(MultipartFile file,String selectedValue){

		try{

			XSSFSheet sheet = getInputSheetBasedOntype(file,selectedValue);;
			int rowIndexTOStart = getRowIndexNo(sheet);
			XSSFSheet outputSheet = getExcelSheet();

			for (Row row : sheet) {
				if(row.getRowNum() < rowIndexTOStart) {
					continue;
				}

				Cell rateType = row.getCell(6);

				if(rateType != null && rateType.getCellType() == CellType.STRING && 
						rateType.getStringCellValue().trim().equalsIgnoreCase("PAD")) {

					Row outputRow = outputSheet.createRow(outputRowNum++);
					outputRow.createCell(0).setCellValue(row.getCell(0).toString()); 
					outputRow.createCell(1).setCellValue(row.getCell(2).toString());   
					outputRow.createCell(2).setCellValue(row.getCell(3).toString());   
					outputRow.createCell(3).setCellValue(row.getCell(4).toString());   
					outputRow.createCell(4).setCellValue(row.getCell(5).toString());
					outputRow.createCell(5).setCellValue(row.getCell(9).toString()); 
					outputRow.createCell(6).setCellValue(row.getCell(10).toString());
					outputRow.createCell(7).setCellValue(String.format("%.4f",Double.parseDouble(row.getCell(12).getStringCellValue().replace("$","").trim())));

				}

			} 

			//another sheet processing
			additionalDrugServices(file);

			try (FileOutputStream fos = new FileOutputStream(outputPath)) {
				outputWorkbook.write(fos);
				System.out.println("Filtered data written to: " + outputPath);
			}
			return ResponseEntity.ok("Report processed successfully!");

		}
		catch (IOException e) {
			return ResponseEntity.internalServerError().body("Error : " + e.getMessage());

		}
	}

	public void additionalDrugServices(MultipartFile file) {


		try{

			InputStream fis = file.getInputStream();
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(1);
			//XSSFSheet sheet = getInputSheetBasedOntype(file,selectedValue);


			for (Row row : sheet) {		

				Cell pricingIndicator = row.getCell(4);
				Cell rateType = row.getCell(5);
				if(pricingIndicator != null && pricingIndicator.getCellType() == CellType.STRING && 
						rateType.getStringCellValue().trim().equalsIgnoreCase("DEF") &&
						pricingIndicator.getStringCellValue().trim().equalsIgnoreCase("SYSMAN"))

				{



					Row outputRow = outputSheet.createRow(outputRowNum++);
					outputRow.createCell(0).setCellValue(row.getCell(3).toString()); 
					outputRow.createCell(5).setCellValue(getDateFormatted(row.getCell(1))); 
					outputRow.createCell(6).setCellValue("12/31/9999");
					outputRow.createCell(7).setCellValue(0.0000);;

				}
			}
		}
		catch (IOException e) {
			System.out.println("File not found exception");

		}
	}


	public void getPricingIndicator(MultipartFile file) {


		try{

			InputStream fis = file.getInputStream();
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(2);
			//XSSFSheet sheet = getInputSheetBasedOntype(file,selectedValue);

			for (Row row : sheet) {		

				Cell pricingIndicator = row.getCell(4);
				Cell rateType = row.getCell(5);
				if(pricingIndicator != null && pricingIndicator.getCellType() == CellType.STRING && 
						rateType.getStringCellValue().trim().equalsIgnoreCase("DEF") &&
						pricingIndicator.getStringCellValue().trim().equalsIgnoreCase("SYSMAN")||
						pricingIndicator.getStringCellValue().trim().equalsIgnoreCase("PAPRIC")||
						pricingIndicator.getStringCellValue().trim().equalsIgnoreCase("PAY0"))
				{



					Row outputRow = outputSheet.createRow(outputRowNum++);
					outputRow.createCell(0).setCellValue(row.getCell(3).toString()); 
					outputRow.createCell(5).setCellValue(getDateFormatted(row.getCell(1))); 
					outputRow.createCell(6).setCellValue("12/31/9999");
					outputRow.createCell(7).setCellValue(0.0000);;

				}
			}
		}
		catch (IOException e) {
			System.out.println("File not found exception");

		}
	}

	public XSSFSheet getInputSheetBasedOntype(MultipartFile file, String selectedValue) throws IOException {

		InputStream fis = file.getInputStream();
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		XSSFSheet sheet = null;
		String expectedSheetName="";
		switch(selectedValue) {

		case "AMBULANCE":
			expectedSheetName = "EMT JAN_2025 FS";
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				sheet = workbook.getSheetAt(i);
				String sheetName = sheet.getSheetName();

				if (sheetName.equalsIgnoreCase(expectedSheetName)) {
					sheet = workbook.getSheetAt(i);
					break;

				}

			}
			return sheet;

		case "DRUGSERVICES":
			expectedSheetName = "PAD APRIL 2025";
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				sheet = workbook.getSheetAt(i);
				String sheetName = sheet.getSheetName();

				if (sheetName.equalsIgnoreCase(expectedSheetName)) {
					sheet = workbook.getSheetAt(i);
					break;

				}

			}
			return sheet;
		default:
			return null;

		}

	}
	public static XSSFSheet getExcelSheet() {

		Row header = outputSheet.createRow(outputRowNum++);
		header.createCell(0).setCellValue("Code");
		header.createCell(1).setCellValue("Modifier1");
		header.createCell(2).setCellValue("Modifier2");
		header.createCell(3).setCellValue("Modifier3");
		header.createCell(4).setCellValue("Modifier4");
		header.createCell(5).setCellValue("Begin Date");
		header.createCell(6).setCellValue("End Date");
		header.createCell(7).setCellValue("Fee");

		return outputSheet;

	}

	public String getDateFormatted(Cell dateCell) {


		String formattedDate = "";

		if (dateCell != null && dateCell.getCellType() == CellType.NUMERIC) {

			double num = dateCell.getNumericCellValue();
			long longVal = (long) num;  
			String date = String.valueOf(longVal);

			if (date.length() == 8) {

				DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
				DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

				LocalDate localDate = LocalDate.parse(date, inputFormat);
				formattedDate = localDate.format(outputFormat);

			}
			else {

				formattedDate = "Invalid Date";
			}

		}
		return formattedDate;
	}

	public int getRowIndexNo(XSSFSheet sheet) {

		int headerRowIndex =-1;
		String expectedHeader ="Code";

		for(Row row: sheet) {
			for(Cell cell : row) {
				String cellvalue = "";

				if(cell.getCellType() == CellType.STRING) {
					cellvalue=cell.getStringCellValue().trim();
				}

				if(cellvalue.equalsIgnoreCase(expectedHeader)) {
					headerRowIndex = row.getRowNum();
					System.out.println("Row Index No " +headerRowIndex);
					break;
				}
			}
			if (headerRowIndex != -1) {
				break;  
			}

		}
		return headerRowIndex;

	}

}

