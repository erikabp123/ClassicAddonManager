package HelperTools;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter {

    public static Date convertFromCurse(String curseFormat){
        //TODO: Support if curseFormat is in hours elapsed instead of a fixed date
        //TODO: Support if curseFormat is in days elapsed instead of a fixed date

        String cleaned = curseFormat.split(" \\(")[0];

        //TODO: Clean this try up
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd yyyy hh:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            date = dateFormat.parse(cleaned);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static boolean isNewerDate(Date date1, Date date2){
        Log.verbose("Checking if " + date1.toString() + " is a newer date than " + date2.toString());

        if(date1.compareTo(date2) > 0){
            return true;
        }
        return false;
    }

}
