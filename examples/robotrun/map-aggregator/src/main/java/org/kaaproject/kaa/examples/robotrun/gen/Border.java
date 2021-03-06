/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.kaaproject.kaa.examples.robotrun.gen;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Border extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Border\",\"namespace\":\"org.kaaproject.kaa.examples.robotrun.gen\",\"fields\":[{\"name\":\"x\",\"type\":\"int\",\"optional\":true},{\"name\":\"y\",\"type\":\"int\",\"optional\":true},{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"BorderType\",\"symbols\":[\"UNKNOWN\",\"SOLID\",\"FREE\"]},\"optional\":true}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private int x;
   private int y;
   private org.kaaproject.kaa.examples.robotrun.gen.BorderType type;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public Border() {}

  /**
   * All-args constructor.
   */
  public Border(java.lang.Integer x, java.lang.Integer y, org.kaaproject.kaa.examples.robotrun.gen.BorderType type) {
    this.x = x;
    this.y = y;
    this.type = type;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return x;
    case 1: return y;
    case 2: return type;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: x = (java.lang.Integer)value$; break;
    case 1: y = (java.lang.Integer)value$; break;
    case 2: type = (org.kaaproject.kaa.examples.robotrun.gen.BorderType)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'x' field.
   */
  public java.lang.Integer getX() {
    return x;
  }

  /**
   * Sets the value of the 'x' field.
   * @param value the value to set.
   */
  public void setX(java.lang.Integer value) {
    this.x = value;
  }

  /**
   * Gets the value of the 'y' field.
   */
  public java.lang.Integer getY() {
    return y;
  }

  /**
   * Sets the value of the 'y' field.
   * @param value the value to set.
   */
  public void setY(java.lang.Integer value) {
    this.y = value;
  }

  /**
   * Gets the value of the 'type' field.
   */
  public org.kaaproject.kaa.examples.robotrun.gen.BorderType getType() {
    return type;
  }

  /**
   * Sets the value of the 'type' field.
   * @param value the value to set.
   */
  public void setType(org.kaaproject.kaa.examples.robotrun.gen.BorderType value) {
    this.type = value;
  }

  /** Creates a new Border RecordBuilder */
  public static org.kaaproject.kaa.examples.robotrun.gen.Border.Builder newBuilder() {
    return new org.kaaproject.kaa.examples.robotrun.gen.Border.Builder();
  }
  
  /** Creates a new Border RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.examples.robotrun.gen.Border.Builder newBuilder(org.kaaproject.kaa.examples.robotrun.gen.Border.Builder other) {
    return new org.kaaproject.kaa.examples.robotrun.gen.Border.Builder(other);
  }
  
  /** Creates a new Border RecordBuilder by copying an existing Border instance */
  public static org.kaaproject.kaa.examples.robotrun.gen.Border.Builder newBuilder(org.kaaproject.kaa.examples.robotrun.gen.Border other) {
    return new org.kaaproject.kaa.examples.robotrun.gen.Border.Builder(other);
  }
  
  /**
   * RecordBuilder for Border instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Border>
    implements org.apache.avro.data.RecordBuilder<Border> {

    private int x;
    private int y;
    private org.kaaproject.kaa.examples.robotrun.gen.BorderType type;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.examples.robotrun.gen.Border.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.examples.robotrun.gen.Border.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.x)) {
        this.x = data().deepCopy(fields()[0].schema(), other.x);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.y)) {
        this.y = data().deepCopy(fields()[1].schema(), other.y);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.type)) {
        this.type = data().deepCopy(fields()[2].schema(), other.type);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Border instance */
    private Builder(org.kaaproject.kaa.examples.robotrun.gen.Border other) {
            super(org.kaaproject.kaa.examples.robotrun.gen.Border.SCHEMA$);
      if (isValidValue(fields()[0], other.x)) {
        this.x = data().deepCopy(fields()[0].schema(), other.x);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.y)) {
        this.y = data().deepCopy(fields()[1].schema(), other.y);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.type)) {
        this.type = data().deepCopy(fields()[2].schema(), other.type);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'x' field */
    public java.lang.Integer getX() {
      return x;
    }
    
    /** Sets the value of the 'x' field */
    public org.kaaproject.kaa.examples.robotrun.gen.Border.Builder setX(int value) {
      validate(fields()[0], value);
      this.x = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'x' field has been set */
    public boolean hasX() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'x' field */
    public org.kaaproject.kaa.examples.robotrun.gen.Border.Builder clearX() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'y' field */
    public java.lang.Integer getY() {
      return y;
    }
    
    /** Sets the value of the 'y' field */
    public org.kaaproject.kaa.examples.robotrun.gen.Border.Builder setY(int value) {
      validate(fields()[1], value);
      this.y = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'y' field has been set */
    public boolean hasY() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'y' field */
    public org.kaaproject.kaa.examples.robotrun.gen.Border.Builder clearY() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'type' field */
    public org.kaaproject.kaa.examples.robotrun.gen.BorderType getType() {
      return type;
    }
    
    /** Sets the value of the 'type' field */
    public org.kaaproject.kaa.examples.robotrun.gen.Border.Builder setType(org.kaaproject.kaa.examples.robotrun.gen.BorderType value) {
      validate(fields()[2], value);
      this.type = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'type' field has been set */
    public boolean hasType() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'type' field */
    public org.kaaproject.kaa.examples.robotrun.gen.Border.Builder clearType() {
      type = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public Border build() {
      try {
        Border record = new Border();
        record.x = fieldSetFlags()[0] ? this.x : (java.lang.Integer) defaultValue(fields()[0]);
        record.y = fieldSetFlags()[1] ? this.y : (java.lang.Integer) defaultValue(fields()[1]);
        record.type = fieldSetFlags()[2] ? this.type : (org.kaaproject.kaa.examples.robotrun.gen.BorderType) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
