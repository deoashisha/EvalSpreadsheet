package com.dbs.lers.main

import scala.collection.mutable.Map
import com.dbs.main.EvalSpreadsheets
import org.scalatest.FunSuite
import org.junit.Assert._

class EvalSpreadsheetTest extends FunSuite{

  test("EvaluSpreadsheetTest Positive") {
    val inputMap = Map( "A0"->"2",
                        "A1"->"4",
                        "A2"->"1",
                        "A3"->"=A0+A1*A2",
                        "B0"->"=A3*(A0+1)",
                        "B1"->"=B2",
                        "B2"->"0",
                        "B3"->"=A0+1"
                        )
    val outputMap = Map( "A0"->"2.00000",
                        "A1"->"4.00000",
                        "A2"->"1.00000",
                        "A3"->"6.00000",
                        "B0"->"18.00000",
                        "B1"->"0.00000",
                        "B2"->"0.00000",
                        "B3"->"3.00000"
                        )
    println("start testEvalSpreadsheet Positive")
    assertEquals(outputMap,EvalSpreadsheets.processSpreadsheetMap(inputMap))
    println("finish testEvalSpreadsheet Positive")
  }

  test("EvaluSpreadsheetTest Negative 1") {
    val inputMap = Map( "A0"->"2",
                        "A1"->"4",
                        "A2"->"1",
                        "A3"->"=A0+A1*B0",
                        "B0"->"=A3*(A0+1)",
                        "B1"->"=B2",
                        "B2"->"0",
                        "B3"->"=A0+1"
                        )
    println("start testEvalSpreadsheet Negative 1")
    assertEquals(true,EvalSpreadsheets.processSpreadsheetMap(inputMap).valuesIterator.contains("CYCLIC DEPENDENCY"))
    println("finish testEvalSpreadsheet Negative 1")
  }
    test("EvaluSpreadsheetTest Negative 2") {
    val inputMap = Map( "A0"->"2",
                        "A1"->"4",
                        "A2"->"1",
                        "A3"->"=A0+A1*B3",
                        "B0"->"=A3*(A0+1)",
                        "B1"->"=B2",
                        "B2"->"0",
                        "B3"->"=B0+1"
                        )
    println("start testEvalSpreadsheet Negative 2")
    assertEquals(true,EvalSpreadsheets.processSpreadsheetMap(inputMap).valuesIterator.contains("CYCLIC DEPENDENCY"))
    println("finish testEvalSpreadsheet Negative 2")
  }

}
