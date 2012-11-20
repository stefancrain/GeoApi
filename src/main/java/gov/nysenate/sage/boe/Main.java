package gov.nysenate.sage.boe;

import gov.nysenate.sage.boe.StreetFiles.NTS;
import gov.nysenate.sage.util.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Main {

    public static void main(String[] args) throws Exception {
        Resource config = new Resource();

        MysqlDataSource db = new MysqlDataSource();
        db.setUser(config.fetch("db.user"));
        db.setPassword(config.fetch("db.pass"));
        db.setServerName(config.fetch("db.host"));
        db.setDatabaseName(config.fetch("db.name"));

        ArrayList<StreetFile> street_files = new ArrayList<StreetFile>();

        File base_dir = new File(config.fetch("street_file.data"));

        street_files.add(new NTS(1, new File(base_dir, "Albany_County_Street_Index.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(2, new File(base_dir, "Allegany_County_Streets.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(3,new File(base_dir, "broome_county_street_list.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                //city_council_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(4,new File(base_dir, "Cattaraugus_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
                //camp_code = (matcher.group(18) != null ? matcher.group(18).trim() : "");
            }
        });


        street_files.add(new NTS(5,new File(base_dir, "Cayuga_County_Street_File.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(6,new File(base_dir, "Chautauqua_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
                // other4 = (matcher.group(18) != null ? matcher.group(18).trim() : "");
            }
        });

        street_files.add(new NTS(7,new File(base_dir, "Chemung_County_8.20.12_Street_File_.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setCityCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(8,new File(base_dir, "Chenango_County.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
            }
        });

        // Had to remove a random tick mark
        street_files.add(new NTS(9, new File(base_dir, "Clinton_County_Street_File.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        // Had to remove a random tick mark, ???
        street_files.add(new NTS(10,new File(base_dir, "Columbia_County_2012StreetIndex.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(11,new File(base_dir, "Cortland_County_Street_file.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(12,new File(base_dir, "Delaware_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        // TODO: Deleted 3 addresses marked with 'Old #' in the next line. Is this okay?
        // TODO: Also removed one address marked with Pre 9/11 Number
        // TODO: Also removed address marked with MUST ADD "129 STRINGHAM \nRD" IN ADDRESS 2
        // TODO: Also removed address marked with PRIVATE ROAD @ 288 SYLVAN LAKE RD
        // TODO: Also removed address marked with PUTNAM COUNTY ?
        street_files.add(new NTS(13,new File(base_dir, "Dutches_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(16,new File(base_dir, "Franklin_County_Streets.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
            }
        });

        street_files.add(new NTS(18,new File(base_dir, "Genesee_County_StreetReportGenesee61st.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        // TODO: Deleted a 1 after OLD STATE ROUTE 23, is this okay?
        street_files.add(new NTS(19,new File(base_dir, "Greene_County_street_file.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setClegCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        // TODO: Deleted TOWN OF HOPE from address NYS ROUTE 30
        // TODO: PEACEFUL FALLS ROAD NORTH SPUR ??
        // TODO: RT 28/RAQUETTE BROOK HILL ??
        // TODO: RT 28 & 30 ??
        street_files.add(new NTS(20,new File(base_dir, "Hamilton_County_Street_Files_Sept_18_2012.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(21,new File(base_dir, "Herkimer_County_File1of2_StreetFinder49thDist.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                // cor_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(21,new File(base_dir, "Herkimer_County_File2of2_StreetFinder51stDist.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                // cor_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(22,new File(base_dir, "Jefferson_County__Senate_Streets.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(24,new File(base_dir, "Livingston_County_streetfiles.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                // ved_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(25,new File(base_dir, "Madison_County_senate_street_file.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setFireCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                // csu_code = (matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(26,new File(base_dir, "Monroe_County_Street_Finder_Report_-_20120828.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                // cvw_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        // Had to remove "CREEK ST             13317      75 -       79" Inclusive because it didn't have town or district entries
        // Removed "FURMEN RD        12072       0-        0 Inclusive" because it lacked a town name
        // Removed two almost completely blank lines in the beginning of this file
        street_files.add(new NTS(27,new File(base_dir, "Montgomery_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                // cor_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(29,new File(base_dir, "Niagara_County_2012_STREET_FINDER.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setClegCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                // cor_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(30,new File(base_dir, "Oneida_County_Street_File.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });


        street_files.add(new NTS(31,new File(base_dir, "Onondaga_County_streets.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setCityCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setClegCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
                //tnwd_code = (matcher.group(18) != null ? matcher.group(18).trim() : "");
            }
        });

        // Deleted, "COURTRIGHT RD     12771        0-       0 Inclusive" had no other info
        // Fixed by adding zipcode "DE SANCATIS DR                 1-       50 Inclusive           Woodbury                          WO   000    002     18    039   099   014   014"
        street_files.add(new NTS(33, new File(base_dir, "Orange_County_Street_List-allin1.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
            }
        });

        // Removed random string BYRON from page 42
        street_files.add(new NTS(34, new File(base_dir, "Orleans_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                // No extra districts
            }
        });


        street_files.add(new NTS(35,new File(base_dir, "Oswego_County_street_file.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });


        street_files.add(new NTS(36,new File(base_dir, "Otsego_County_STREET_FINDER_8-29-12.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(37,new File(base_dir, "Putnam_County_8-30-12_STREET_FINDER_REPORT.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
                // libr_code = (matcher.group(18) != null ? matcher.group(18).trim() : "");
            }
        });

        street_files.add(new NTS(38,new File(base_dir, "Rensselaer_County_STREET_FINDER.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setCityCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
                addressRange.setFireCode(matcher.group(18) != null ? matcher.group(18).trim() : "");
            }
        });

        street_files.add(new NTS(39,new File(base_dir, "Rockland_County_streets.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setClegCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(40,new File(base_dir, "StLawrence_County_All_Streets_SLC.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        // Deleted 3 incomplete lines from the beginning of this file.
        street_files.add(new NTS(41,new File(base_dir, "Saratoga_County_STREET_FILE.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
            }
        });

        street_files.add(new NTS(42,new File(base_dir, "Schenectady_County_46th_Senate_District-file1.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(42,new File(base_dir, "Schenectady_County_49th_Senate_District-file2.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        // Fix one missing postal address for PETERSON DRIVE
        street_files.add(new NTS(44,new File(base_dir, "Schuyler_County_Street_Finder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
            }
        });

        street_files.add(new NTS(45,new File(base_dir, "Seneca_County_Street_Finder_List_for_Seneca_County_.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setClegCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(46,new File(base_dir, "Steuben_County_StreetFinder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(49,new File(base_dir, "Tioga_county_street.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                // judc_code = (matcher.group(15) != null ? matcher.group(15).trim() : "");
                // legl_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
                addressRange.setVillCode(matcher.group(18) != null ? matcher.group(18).trim() : "");
            }
        });

        street_files.add(new NTS(50,new File(base_dir, "Tompkins_County_Streetfinder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setClegCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setFireCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        // DELETED two incomplete lines in the beginning of the file
        // DELETED SUNSET RD         CAMP      155 -   230 Inclusive              PLATTEKILL                       PLA   000    006    019    039   103   052   009          121
        // because it has CAMP as a zipcode..WHAT?
        street_files.add(new NTS(51,new File(base_dir, "Ulster_County_Street_File.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setClegCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setVillCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                // oth3_code = (matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        // DELETED 1 incomplete line from beginning of file
        street_files.add(new NTS(52,new File(base_dir, "Warren_County_Street_Finder_8-29-2012.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setClegCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                // cor_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });


        // DELETED 1 incomplete line in the beginning of the file
        street_files.add(new NTS(53,new File(base_dir, "Washington_County_Street_File.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                // jud_code = (matcher.group(16) != null ? matcher.group(16).trim() : "");
            }
        });

        street_files.add(new NTS(54,new File(base_dir, "Wayne_County_StreetFinder.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                addressRange.setVillCode(matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                // judc_code = (matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        street_files.add(new NTS(57,new File(base_dir, "Yates_County_Street_File_2012.txt")) {
            @Override
            public void store_extra_districts(BOEAddressRange addressRange, Matcher matcher) {
                // legl_code = (matcher.group(15) != null ? matcher.group(15).trim() : "");
                addressRange.setFireCode(matcher.group(16) != null ? matcher.group(16).trim() : "");
                addressRange.setVillCode(matcher.group(17) != null ? matcher.group(17).trim() : "");
            }
        });

        for (StreetFile file : street_files) {
            //StreetFile file = street_files.get(street_files.size()-1);
            System.out.println("Saving "+file.street_file.getAbsolutePath());
            file.clear(db);
            file.save(db);
        }
    }

}

