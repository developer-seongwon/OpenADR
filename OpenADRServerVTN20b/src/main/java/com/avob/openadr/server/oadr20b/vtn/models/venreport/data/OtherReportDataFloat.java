package com.avob.openadr.server.oadr20b.vtn.models.venreport.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "otherreportdata_float")
public class OtherReportDataFloat extends ReportData {

	private Float value;

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

}
