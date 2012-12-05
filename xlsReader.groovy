import groovy.ui.Console
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.hssf.usermodel.HSSFRow
import groovy.io.FileType

@Grab(group = 'org.apache.poi', module = 'poi-scratchpad', version = '3.8-beta3')

// package extract.excel

import org.apache.poi.hssf.usermodel.HSSFWorkbook

/**
 * Groovy Builder that extracts data from
 * Microsoft Excel spreadsheets.
 * @author Goran Ehrsson
 * @url http://www.technipelago.se/content/technipelago/blog/44
 */
class ExcelBuilder {

	def workbook
	def labels
	def row

	ExcelBuilder(String fileName) {
		HSSFRow.metaClass.getAt = { int idx ->
			def cell = delegate.getCell(idx)
			if(!cell) {
				return null
			}
			def value
			switch(cell.cellType) {
				case HSSFCell.CELL_TYPE_NUMERIC:
					if(HSSFDateUtil.isCellDateFormatted(cell)) {
						value = cell.dateCellValue
					} else {
						value = cell.numericCellValue
					}
					break
				case HSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.booleanCellValue
					break
				default:
					value = cell.stringCellValue
					break
			}
			return value
		}

		new File(fileName).withInputStream { is ->
			workbook = new HSSFWorkbook(is)
		}
	}

	def getSheet(idx) {
		def sheet
		if(!idx) idx = 0
		if(idx instanceof Number) {
			sheet = workbook.getSheetAt(idx)
		} else if(idx ==~ /^\d+$/) {
			sheet = workbook.getSheetAt(Integer.valueOf(idx))
		} else {
			sheet = workbook.getSheet(idx)
		}
		return sheet
	}

	def cell(idx) {
		if(labels && (idx instanceof String)) {
			idx = labels.indexOf(idx.toLowerCase())
		}
		return row[idx]
	}

	def propertyMissing(String name) {
		cell(name)
	}

	def eachLine(Map params = [:], Closure closure) {
		def offset = params.offset ?: 0
		def max = params.max ?: 9999999
		def sheet = getSheet(params.sheet)
		def rowIterator = sheet.rowIterator()
		def linesRead = 0

		if(params.labels) {
			labels = rowIterator.next().collect { it.toString().toLowerCase() }
		}
		offset.times { rowIterator.next() }

		closure.setDelegate(this)

		while(rowIterator.hasNext() && linesRead++ < max) {
			row = rowIterator.next()
			closure.call(row)
		}
	}
}

LinkedHashMap processXlsFile(xlsfile){
	def xls = new ExcelBuilder(xlsfile);

	def res = [uugName: '', gdsName: '', params: [] as Set<String>];

	xls.eachLine([sheet: 0]) {HSSFRow row->
		switch(cell(0)){
			case 'Имя узла':
				res.uugName = cell(1);
				break;
			case 'ГРС':
				res.gdsName = cell(1);
				break;
		}
	}

	xls.eachLine([labels: true, sheet: 1]) {HSSFRow row->
	//	println "Device = $Device; Tag name: ${cell('Tag name')};"
		if ( (cell('Tag name') =~ /$Device\.dev\./) ){
	//		params << "$gdsName;$uugName.${(cell('Tag name') ==~ /((?:[^\.]\.){2}[^\.])/)[0][1]}";
			res.params << "${res.gdsName};${res.uugName}.${(cell('Tag name') =~ /([^.]+\.){2}[^.]+/)[0][0]}";
		}
	}

	res;
}

//String testfile = '/home/pasha/temp/2012-09-14_GTE_MI_SELEN.xls'
//println processXlsFile(testfile);

def dir = '/mnt/net/p/users/frolova/Структура/';
def infotehFile = '/home/pasha/temp/Измерительный трубопровод УИ газа.xls';

def xlsInfoteh = new ExcelBuilder(infotehFile);
List infotehData = [];
xlsInfoteh.eachLine([labels: true]){HSSFRow row->
	infotehData << [
		'id Инфотех': cell('id Инфотех')
		,'Наименование': cell('Наименование')
		,'ID УИ газа': cell('ID УИ газа')
		,'УИ газа': cell('УИ газа')
		,'Dу условный': cell('Dу условный')
		,'Pразр': cell('Pразр')
		,'Qmax проектн.': cell('Qmax проектн.')
		,'Qmin проектн.': cell('Qmin проектн.')
		,'Номер ИТ': cell('Номер ИТ')
		,'Pmin проектн.': cell('Pmin проектн.')
		,'Pmax проектн.': cell('Pmax проектн.')
		,'Абсолютное давление газа': cell('Абсолютное давление газа')
	];
}
//println infotehData;

Integer parseInfotehChannel(String str){
	try{
		def channel = (str =~ /(?ixu)
		(?: (?:ИТ|нитка|Измерительный\sтрубопровод|н\.)\W*(?<postChannel>\d) )
		|
		(?: (?<preChannel>\d)\W*(?:ИТ|нитка|Измерительный\sтрубопровод|н\.) )
		|
		(?:^\W*(\d)\W*$)
	/)[0];

//	println "Channel=${(channel - null)[1]}"
	return (channel - null)[1].toInteger();
	}
	catch (java.lang.IndexOutOfBoundsException ignore){ -1 }
}

List results = [];

new File(dir).eachFileMatch(FileType.FILES, ~/(?i).+\.xls/) {File file->
//new File(dir).eachFileMatch(FileType.FILES, ~/2012-10-19_GTE_SH_KARGOPOLYE.xls/) {File file->
	try{
		def param = processXlsFile(file.absolutePath);

		param.params.sort().eachWithIndex{p, ind->
			def infotehFound = infotehData.findAll{
				try{
					(
					it.'Наименование'.split(/,/)[1].replaceAll(/ /, '').trim() == param.gdsName.replace(/ГРС(?:\W*\d)? /, '').replaceAll(/ /, '').trim()
					&&
					parseInfotehChannel(it.'Номер ИТ') in [-1, p[-1].toInteger()]
					)
				}
				catch(java.lang.ArrayIndexOutOfBoundsException ignore){ false }
			}.sort{ it.'id Инфотех' };

			if (1 == infotehFound.size() && 1 == param.params.size()){
				println "p=$p - single mapped by GDS"
			}
			else{

			}
/*
		println "\tInfoteh data:"
		infotehFound.eachWithIndex{entry, i ->
			println "\t\t$i) ${entry}"
		}
*/

//			println "${file.name};$p";
			try{
				println "${file.name};$p;${infotehFound[ind].values().collect{ it instanceof Double ? it.toLong() : it }.join(';')};${param.params.size()}=${infotehFound.size()}";
//				results << [ param: param, infoteh: infotehFound[ind] ];
			}
			catch(java.lang.NullPointerException ignore){ // Not infoteh found
				println "${file.name};$p;No infoteh record found(${param.params.size()})";
			}
		};
	}
	catch(Exception e){
		println "Error happened process file ${file}: " + e;
	}
}

// http://groovy.codehaus.org/Groovy+Console
//Console console = new Console([results: results, infotehData: infotehData] as Binding);
//console.run();

/*
println "Not mapped Infoteh records:";
//println infotehData[0..2].findAll{data->
println infotehData.findAll{data->
	results.find{res->
		!(data.'id Инфотех' in res.infoteh.'id Инфотех')
	}
}.collect{ it.values().collect{ it instanceof Double ? it.toLong() : it }.join(';') }.join('\n')
*/