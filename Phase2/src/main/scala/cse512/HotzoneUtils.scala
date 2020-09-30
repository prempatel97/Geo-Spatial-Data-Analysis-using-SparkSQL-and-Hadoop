package cse512

object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String ): Boolean = {
    // YOU NEED TO CHANGE THIS PART
    val Array(rx1,ry1,rx2,ry2) = queryRectangle.split(",").map(_.toDouble)
    val Array(px1,py1) = pointString.split(",").map(_.toDouble)
    if(px1>=rx1 && px1<=rx2 && py1>=ry1 && py1<=ry2){
      return true
    }
    else{
      return false
    } // YOU NEED TO CHANGE THIS PART
  }

  // YOU NEED TO CHANGE THIS PART

}
