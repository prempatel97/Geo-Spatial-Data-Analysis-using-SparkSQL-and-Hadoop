package cse512

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

object HotcellUtils {
  val coordinateStep = 0.01

  def CalculateCoordinate(inputString: String, coordinateOffset: Int): Int =
  {
    // Configuration variable:
    // Coordinate step is the size of each cell on x and y
    var result = 0
    coordinateOffset match
    {
      case 0 => result = Math.floor((inputString.split(",")(0).replace("(","").toDouble/coordinateStep)).toInt
      case 1 => result = Math.floor(inputString.split(",")(1).replace(")","").toDouble/coordinateStep).toInt
      // We only consider the data from 2009 to 2012 inclusively, 4 years in total. Week 0 Day 0 is 2009-01-01
      case 2 => {
        val timestamp = HotcellUtils.timestampParser(inputString)
        result = HotcellUtils.dayOfMonth(timestamp) // Assume every month has 31 days
      }
    }
    return result
  }

  def timestampParser (timestampString: String): Timestamp =
  {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val parsedDate = dateFormat.parse(timestampString)
    val timeStamp = new Timestamp(parsedDate.getTime)
    return timeStamp
  }

  def dayOfYear (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_YEAR)
  }

  def dayOfMonth (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_MONTH)
  }

  // YOU NEED TO CHANGE THIS PART

  def getisordscore(numNeighbours: Int, sum_xj: Int, numCells: Int, x: Int, y: Int, z: Int, X_bar: Double, S: Double): Double = {
    val num = sum_xj.toDouble - (X_bar * numNeighbours.toDouble)
    val denom1 = numCells.toDouble * numNeighbours.toDouble
    val denom2 = numNeighbours.toDouble * numNeighbours.toDouble
    val denom3 = numCells.toDouble - 1.0
    val denom = S * scala.math.sqrt((denom1 - denom2) / denom3)
    val result = num / denom
    return result
  }

  def neighbourCount (x: Int, y:Int, z:Int, minX: Int, maxX: Int, minY: Int, maxY: Int, minZ: Int, maxZ: Int): Int =
  {
    var count = 26
    var boundary_x = 0
    var boundary_y = 0

    if(x == minX || x == maxX) {
      count = count - 9
      boundary_x = 1
    }
    if(y == minY || y == maxY){
      count = count - 9
      boundary_y = 1
      if(boundary_x == 1)
        count = count + 3
    }
    if(z == minZ || z == maxZ){
      count = count - 9
      if(boundary_x == 1 && boundary_y == 1)
        count = count + 5
      else if(boundary_x == 1 || boundary_y == 1)
        count = count + 3
    }
    val result = count
    return result
  }



  def areNeighbours(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Boolean = {
    return ((x2 == x1+1 || x2 == x1 || x2 == x1-1) && (y2 == y1+1 || y2 == y1 || y2 == y1-1) && (z2 == z1+1 || z2 == z1 || z2 == z1-1))
  }

  def squareOf (i: Int): Double = ((i * i).toDouble)

  def squareDouble(v: Double): Double = v*v

}
