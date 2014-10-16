package edu.buffalo.cse.irf14.util;

/**
 * Enum for month 
 * @author sghodke, amitpuru
 */
public enum Month {
	
	JANUARY("01"),
	FEBRUARY("02"),
	MARCH("03"),
	APRIL("04"),
	MAY("05"),
	JUNE("06"),
	JULY("07"),
	AUGUST("08"),
	SEPTEMBER("09"),
	OCTOBER("10"),
	NOVEMBER("11"),
	DECEMBER("12"),
	JAN("01"),
	FEB("02"),
	MAR("03"),
	APR("04"),
	JUN("06"),
	JUL("07"),
	AUG("08"),
	SEP("09"),
	OCT("10"),
	NOV("11"),
	DEC("12");
	
	private String value;
	
	private Month(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
