package com.dbs.main

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

import scala.annotation.tailrec
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.io.Source
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

import org.apache.commons.cli.BasicParser
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.Options


object EvalSpreadsheets {
  
  /**
   * Input Parameters
   * @inputFileName - Input File Name
   * @OutputFileName - Output File Name
   */
  var inputFileName:String=null;
  var outputFileName:String=null;
  
  val toolbox = currentMirror.mkToolBox()
  var rows:Int=0
    def main(args: Array[String]) {
      try{
        parseCommandLineAgrs(args)
        val inputData: Map[String,String] = csvToCellMap(inputFileName)
        val outputData = processSpreadsheetMap(inputData)
        if(!outputData.valuesIterator.contains("CYCLIC DEPENDENCY")) writeFile(outputFileName, outputData)
      } catch {
        case _: Throwable => println(" Exception in Input Data Please check Input File ")
      }
  }

  /**
   * processSpreadsheetMap takes input as Map to Process Spreadsheet data.
   * identifies CYCLIC Dependency in data and reports as output in console
   */
  def processSpreadsheetMap(inputMap: Map[String,String]): Map[String,String] = {
    inputMap.foreach(x => inputMap+= (x._1 -> recursiveReplace(x._1,x._2, inputMap)))
    if(inputMap.valuesIterator.contains("CYCLIC DEPENDENCY")) println("CYCLIC DEPENDENCY Found in Input Data. Please check the File")
    inputMap
  }
  
  /**
   * recursiveReplace takes input as key and value as cell data with map of spreadsheet
   * it returns a string with value of the variables replaced in equation passed as parameter 
   */
  @tailrec
    def recursiveReplace(key:String,value : String,equationMap:Map[String,String]):String = {
          var newValue = value
            val variablereplaced = value.replaceAll("=", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll("\\+", ",")
                .replaceAll("\\-", ",")
                .replaceAll("\\*", ",")
                .replaceAll("\\/", ",")
            if(variablereplaced.contains(key)){
              s"CYCLIC DEPENDENCY"
            }else{
                if(newValue.contains("CYCLIC DEPENDENCY")){
                  "CYCLIC DEPENDENCY"
                } else if(variablereplaced.replaceAll(",","").replaceAll("\\.","").forall(Character.isDigit)){
                  textFormatter(toolbox.eval(toolbox.parse(newValue.replaceAll("=",""))).toString)
                }else {
                  variablereplaced.split(",").toList.foreach(f => newValue = newValue.replaceAll(f, equationMap.getOrElse(f,f)))
                  recursiveReplace(key,newValue,equationMap)
                }
            }
    }
    /**
     * textFormatter formats the digits in required format i.e #.#####
     */
    def textFormatter(x: String) = if(x forall Character.isDigit) f"${x.toDouble}%1.5f" else x+"0000"
    
    /**
     * csvToCellMap converts data in file to Map of spreadsheet with cell id's as key and values.
     */
    def csvToCellMap(fileName:String) : LinkedHashMap[String,String] ={
      val inputFile = Source.fromFile(fileName)
      val map = LinkedHashMap.empty[String,String]
      var i = 1
      for (line <- inputFile.getLines) {
        val cols = line.split(",").map(_.trim)
        cols.zipWithIndex.map { case (element, index) => 
         map += (getRowNumberToChar(i)+index.toString -> element)
         }
        i=i+1
    }
      rows = i-1
      map
    }
    
  /**
   * getRowNumberToChar converts rowNumbers to Characters.
   */
  def getRowNumberToChar(i:Integer) : String = {
		return if(i > 0 && i < 27) { String.valueOf((i + 64).toChar) } else null;
	}
  def parseCommandLineAgrs(args: Array[String]) = {
    val opt = new Options()
    opt.addOption("i", true, "external params").addOption("o", true, "external params")
    val parser: CommandLineParser = new BasicParser()
    val cmd = parser.parse(opt, args)
    if (cmd.hasOption("i")) {
      cmd.getOptionValues("i").foreach(x => {
        println(s"InputFile : $x")
        inputFileName=x      })
    }
    if (cmd.hasOption("o")) {
      cmd.getOptionValues("o").foreach(x => {
        println(s"OutputFile : $x")
        outputFileName =x      })
    }
  }
  
  /**
   * writeFile writes Map parameter to the File to file specified. 
   */
  def writeFile(filename: String,map:Map[String,String]): Unit = {
    var outputArray= new ListBuffer[String]()
    for( i <- 1 until rows+1){
      outputArray += map.filter(x => x._1.startsWith(getRowNumberToChar(i))).values.seq.mkString(",")
    }
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- outputArray) {
        bw.write(line)
        bw.newLine()
    }
    bw.close()
    println(s"Output File Generated Successfully. : $outputFileName")
}
}