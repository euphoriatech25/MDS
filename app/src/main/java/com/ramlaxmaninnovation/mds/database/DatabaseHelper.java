//package com.ramlaxmaninnovation.mds.database;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class DatabaseHelper extends SQLiteOpenHelper {
//    public static final String PATIENT_REPORT_TABLE = "patient_report_table";
//    public static final String PATIENT_NAME = "patient_name";
//    public static final String PATIENT_ID = "patient_id";
//    public static final String PATIENT_REMARKS = "patient_remarks";
//    public static final String PATIENT_DOSE_CONSUMPTION_TIME = "patient_dose_consumption_time";
//    private static final String DATABASE_NAME = "patientDb";
//    private static final int DATABASE_VERSION = 1;
//    private Context cntx;
//
//
//    public DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        cntx = context;
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        createTables(db);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        dropTables(db);
//    }
//
//    @Override
//    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        dropTables(db);
//
//    }
//
//    private void dropTables(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS " + PATIENT_REPORT_TABLE);
//        createTables(db);
//
//    }
//
//    private void createTables(SQLiteDatabase db) {
//
//        db.execSQL("CREATE TABLE " + PATIENT_REPORT_TABLE + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + PATIENT_ID + " TEXT," +
//                PATIENT_NAME + " TEXT,"+
//                PATIENT_REMARKS + " TEXT,"
//                + PATIENT_DOSE_CONSUMPTION_TIME + " TEXT"
//                + ")");
//    }
//
//    public String getPatientLatestDetails(String patient_id,String patient_name) {
//        String last_consumption_date ="";
//
////        Cursor c = getReadableDatabase().rawQuery("SELECT " + PATIENT_DOSE_CONSUMPTION_TIME + " FROM " + PATIENT_REPORT_TABLE +" WHERE " + "PATIENT_NAME "+" = "+patient_name +" AND "+ " PATIENT_ID "+" = "+ patient_id , null);
//        Cursor c = getReadableDatabase().rawQuery("SELECT " + PATIENT_DOSE_CONSUMPTION_TIME + " FROM " + PATIENT_REPORT_TABLE + " WHERE " + PATIENT_ID + " =" + patient_id, null);
//
//        try {
//
//            while (c.moveToNext()) {
//                last_consumption_date = c.getString(c.getColumnIndex(PATIENT_DOSE_CONSUMPTION_TIME));
//            }
//        } finally {
//            if (c != null)
//                c.close();
//        }
//        return last_consumption_date;
//    }
//
//    public void insertRooms(PatientReportModel patientReportModel) {
//        ContentValues contentValues = new ContentValues();
//        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
//        contentValues.put(PATIENT_ID, patientReportModel.patient_id);
//        contentValues.put(PATIENT_NAME, patientReportModel.patient_name);
//        contentValues.put(PATIENT_REMARKS, patientReportModel.patient_remarks);
//        contentValues.put(PATIENT_DOSE_CONSUMPTION_TIME, patientReportModel.patient_dose_consumption_time);
//        sqLiteDatabase.insert(PATIENT_REPORT_TABLE, null, contentValues);
//    }
//
//
//    public void clearTable() {
//        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
//        sqLiteDatabase.execSQL(" Delete from " + PATIENT_REPORT_TABLE);
//        sqLiteDatabase.execSQL(" DELETE FROM SQLITE_SEQUENCE WHERE name='" + PATIENT_REPORT_TABLE + "';");
//
//        sqLiteDatabase.close();
//    }
//
//    public List<PatientReportModel> patientReportModelList() {
//
//        List<PatientReportModel> patientReportModelLists = new ArrayList<>();
//        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM  PATIENT_REPORT_TABLE ORDER BY PATIENT_DOSE_CONSUMPTION_TIME DESC ", null);
//        try {
//            while (c.moveToNext()) {
//                PatientReportModel patientReportModelList = new PatientReportModel();
//                patientReportModelList.patient_id = c.getString(c.getColumnIndex(PATIENT_ID));
//                patientReportModelList.patient_name = c.getString(c.getColumnIndex(PATIENT_NAME));
//                patientReportModelList.patient_remarks = c.getString(c.getColumnIndex(PATIENT_REMARKS));
//                patientReportModelList.patient_dose_consumption_time = c.getString(c.getColumnIndex(PATIENT_DOSE_CONSUMPTION_TIME));
//                patientReportModelLists.add(patientReportModelList);
//
//            }
//            c.close();
//                return patientReportModelLists;
//            } finally{
//                if (c != null)
//                    c.close();
//            }
//        }
//    }
