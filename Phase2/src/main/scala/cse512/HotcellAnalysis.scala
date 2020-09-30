package cse512

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

object HotcellAnalysis {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)

def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame =
{
  // Load the original data from a data source
  var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter",";").option("header","false").load(pointPath);
  pickupInfo.createOrReplaceTempView("nyctaxitrips")
  /*Often we might want to store the spark Data frame as the table and query it,
  to convert Data frame into temporary view that is available for only that spark session,
  we use registerTempTable or createOrReplaceTempView (Spark > = 2.0) on our spark Dataframe.
   */
  pickupInfo.show()

  // Assign cell coordinates based on pickup points
  spark.udf.register("CalculateX",(pickupPoint: String)=>((
    HotcellUtils.CalculateCoordinate(pickupPoint, 0)
    )))
  spark.udf.register("CalculateY",(pickupPoint: String)=>((
    HotcellUtils.CalculateCoordinate(pickupPoint, 1)
    )))
  spark.udf.register("CalculateZ",(pickupTime: String)=>((
    HotcellUtils.CalculateCoordinate(pickupTime, 2)
    )))
  pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
  var newCoordinateName = Seq("x", "y", "z")
  /*Seq is a trait which represents indexed sequences*/
  pickupInfo = pickupInfo.toDF(newCoordinateName:_*)
  pickupInfo.createOrReplaceTempView("pickupPoints")
  pickupInfo.show()

  // Define the min and max of x, y, z
  val minX = -74.50/HotcellUtils.coordinateStep
  val maxX = -73.70/HotcellUtils.coordinateStep
  val minY = 40.50/HotcellUtils.coordinateStep
  val maxY = 40.90/HotcellUtils.coordinateStep
  val minZ = 1
  val maxZ = 31
  val numCells = (maxX - minX + 1)*(maxY - minY + 1)*(maxZ - minZ + 1)

  // YOU NEED TO CHANGE THIS PART

  spark.udf.register("getisordscore", (numNeighbours: Int, sum_xj: Int, numCells: Int, x: Int, y: Int, z: Int, X_bar: Double, S: Double) => ((
    HotcellUtils.getisordscore(numNeighbours, sum_xj, numCells, x, y, z, X_bar, S)
    )))

  spark.udf.register("neighbourCount", (xval: Int, yval: Int, zval: Int, minX: Int, maxX: Int, minY: Int, maxY: Int, minZ: Int, maxZ: Int) => (
    HotcellUtils.neighbourCount(xval, yval, zval, minX, minY, minZ, maxX, maxY, maxZ)
    ))

  spark.udf.register("areNeighbours", (xval1: Int, yval1: Int, zval1: Int, xval2: Int, yval2: Int, zval2: Int) => (
    HotcellUtils.areNeighbours(xval1, yval1, zval1, xval2, yval2, zval2)
    ))

  spark.udf.register("squareOf", (i: Int) =>(
    HotcellUtils.squareOf(i)
    ))

  val celldata1 = spark.sql("select x, y, z"
    + " from pickupPoints"
    + " where x >= " + minX + " and x <= " + maxX + " and y >= " + minY + " and y <= " + maxY + " and z >= " + minZ + " and z <= " + maxZ
    + " order by z, y, x")
  celldata1.createOrReplaceTempView("cellView1")

  val celldata2 = spark.sql("select x, y, z, count(*) as xj"
    + " from cellView1"
    + " group by x, y, z"
    + " order by z, y, x")
  celldata2.createOrReplaceTempView("cellView2")

  val attributes = spark.sql("select sum(xj), sum(squareOf(xj))"
    + " from cellView2")
  attributes.createOrReplaceTempView("attributes")

  val sum_xj = attributes.first().getLong(0).toDouble;
  val sum_squares_xj = attributes.first().getDouble(1);
  val n = numCells.toDouble
  val X_bar = sum_xj / n
  val S = scala.math.sqrt((sum_squares_xj.toDouble / n) - (HotcellUtils.squareDouble(X_bar.toDouble)))

  val neighbour = spark.sql("select R.x as x, R.y as y, R.z as z, neighbourCount(R.x, R.y, R.z, " + minX + "," + maxX + "," + minY + "," + maxY + "," + minZ + "," + maxZ + ") as numNeighbours, sum(S.xj) as sum_xj"
    + " from cellView2 as R, cellView2 as S"
    + " where areNeighbours(R.x, R.y, R.z, S.x, S.y, S.z)"
    + " group by R.z, R.y, R.x"
    + " order by R.z, R.y, R.x")
  neighbour.createOrReplaceTempView("neighbour")

  val score = spark.sql("select x, y, z, getisordscore(numNeighbours, sum_xj, "+ numCells + ", x, y, z," + X_bar + ", " + S + ") as zscore"
    + " from neighbour"
    + " order by zscore DESC");
  score.createOrReplaceTempView("score")

  val result = spark.sql("select x, y, z"
    + " from score "
    + " limit 50")
  result.createOrReplaceTempView("result")

  // YOU NEED TO CHANGE THIS PART
  return result
  }
}
