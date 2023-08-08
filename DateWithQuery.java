package mysql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateWithQuery {
    private List<Date> dates;
    private String query;

    public DateWithQuery(List<Date> dates, String query) {
        this.dates = dates;
        this.query = query;
    }

    public List<Date> getDates() {

        return dates;
    }

    public String getQuery() {
        return query;
    }
}

