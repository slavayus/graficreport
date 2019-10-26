package com.ifmo.graficreport.apparapi.oopnbody;


public final class Body{
   public static Body[] allBodies;

   final private boolean isHeavy;

   public Body(float x, float y, float z, float _m, boolean isHeavy) {
      this.x = x;
      this.y = y;
      this.z = z;
      m = _m;
      this.isHeavy = isHeavy;
   }

   float x, y, z, m, vx, vy, vz;

   public boolean isHeavy() {
      return isHeavy;
   }

   public float getX() {
      return x;
   }

   public float getY() {
      return y;
   }

   public float getZ() {
      return z;
   }

   public float getRadius() {
      return (float) Math.sqrt(x * x + y * y + z* z);
   }

   public float getTheta() {
      return (float) Math.acos(z / getRadius());
   }

   public float getPhi() {
      return (float) Math.atan(y / x);
   }

   public float getVx() {
      return vx;
   }

   public float getVy() {
      return vy;
   }

   public float getVz() {
      return vz;
   }

   public float getM() {
      return m;
   }

   public void setM(float _m) {
      m = _m;
   }

   public void setX(float _x) {
      x = _x;
   }

   public void setY(float _y) {
      y = _y;
   }

   public void setZ(float _z) {
      z = _z;
   }

   public void setRadius(float radius) {
      final float theta = getTheta();
      final float phi = getPhi();
      x = (float) (radius * Math.cos(theta) * Math.sin(phi));
      y = (float) (radius * Math.sin(theta) * Math.sin(phi));
      z = (float) (radius * Math.cos(phi));
   }

   public void setTheta(float theta) {
      final float radius = getRadius();
      final float phi = getPhi();
      x = (float) (radius * Math.cos(theta) * Math.sin(phi));
      y = (float) (radius * Math.sin(theta) * Math.sin(phi));
      z = (float) (radius * Math.cos(phi));
   }

   public void setPhi(float phi) {
      final float radius = getRadius();
      final float theta = getTheta();
      x = (float) (radius * Math.cos(theta) * Math.sin(phi));
      y = (float) (radius * Math.sin(theta) * Math.sin(phi));
      z = (float) (radius * Math.cos(phi));
   }

   public void setVx(float _vx) {
      vx = _vx;
   }

   public void setVy(float _vy) {
      vy = _vy;
   }

   public void setVz(float _vz) {
      vz = _vz;
   }
}
