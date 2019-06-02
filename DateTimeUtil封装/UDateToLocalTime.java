package cn.proflu.profluweb.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class UDateToLocalTime {

	public static LocalDateTime getDateToLocalTime( Date  date ) {
	    ZoneId zone = ZoneId.systemDefault();
	    return  LocalDateTime.ofInstant( date.toInstant(), zone);
	}
}
