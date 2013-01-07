package gov.nysenate.sage.scripts;

import gov.nysenate.sage.util.DB;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.SQLException;

public class RebuildTriggers {

    public static void main(String[] args) throws Exception{

        DataSource db = DB.getDataSource();
        QueryRunner runner = new QueryRunner(db);

        System.out.println("Rebuilding Triggers");
        try {
            rebuildSchoolTownTrigger(runner);
        }
        catch( SQLException sqlException){
            System.err.println("Failed to rebuild triggers with error: " + sqlException.getMessage());
        }
    }

    public static void rebuildSchoolTownTrigger( QueryRunner runner ) throws SQLException {

       runner.update("DROP TRIGGER IF EXISTS senate_code_check;");
       runner.update(
            "CREATE TRIGGER senate_code_check BEFORE INSERT ON `street_data`\n" +
            "FOR EACH ROW BEGIN \n" +
            "SET NEW.senate_town_code = \n" +
            "(SELECT `to_code` FROM `street_data_map`\n" +
            " WHERE `map_col` = 'town_code' \n" +
            " AND `key_value` = NEW.county_code\n" +
            " AND TRIM(LEADING '0' FROM `from_code`) = TRIM(LEADING '0' FROM NEW.town_code)\n" +
            ");\n" +
            "SET NEW.senate_school_code = \n" +
            "(SELECT `to_code` FROM `street_data_map`\n" +
            " WHERE `map_col` = 'school_code'\n" +
            " AND `key_value` = NEW.county_code\n" +
            " AND TRIM(LEADING '0' FROM `from_code`) = TRIM(LEADING '0' FROM NEW.school_code)\n" +
            ");\n" +
            "END;\n");
        System.out.println("Rebuilt senate school and town check trigger.");
    }


}