package com.example.ohaneul.etc;

import java.util.Date;

public class Report_post {
    private String category,docID,uid;
    private Date date;
    private String report_email;

    public Report_post(String category, Date date, String report_email, String docID, String uid){
        this.category = category;
        this.date = date;
        this.report_email = report_email;
        this.docID = docID;
        this.uid = uid;
        }
    public String getCategory() {
        return category;
    }
      public void setCategory(String category) {
        this.category = category;
    }    public String getReport_email() {
            return report_email;
        }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public void setReport_email(String report_email) {
            this.report_email = report_email;
        }

    public void setdocID(String docID) {
        this.docID = docID;
    }
    public String getdocID(){
        return docID;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUid(){
        return uid;
    }

}
