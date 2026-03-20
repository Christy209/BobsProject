package Utilities;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Inputs {
    
	private String excelFilePath = System.getProperty("user.dir") + "/Resource/SBData.xlsx";
    @SuppressWarnings("unused")
	private int row; // Row index to read data
    
    // Using a Map to store field values dynamically
    private Map<String, String> dataMap = new HashMap<>();
    
    // Constructor to read values from Excel
    public Inputs(int row, String sheetName) throws Exception {
        this.row = row; // Set row index dynamically
        String[] keys;
        if ("Sheet1".equalsIgnoreCase(sheetName)) {
            keys = new String[] { "menu", "Function","Cifid", "Schmccd", "Acct_Statement", "CalBase", "FreqType", "FreqWeek", "FreqDay",
        				  "FreqStartDD","FreqHldyStat", "DispatchMode", "Modeofoperation", "MinIntPcntCr", "MaxIntPcntCr",
        				  "CustPrefIntCr",  "AcctPrefIntCr", "ChanPrefIntCr", "IntCrAcctFlg", "NextIntCrCalcDt", "Regnum",
        				  "NomineeCifId", "Relation","NomineePcnt","Nominee-Minor","GuardianName","GuardianCode",
        				  "AddressLine1","AddressLine2","City","State","Country","PostalCode","SecondNomineeflg",
        				  "Second_NomineeCifId","Second_Relation","Second_NomineePcnt","Relcode","Second_RelatedPartyflg",
        				  "RelnType","RelnCode","RelatedFreqType", "RelatedFreqWeek","RelatedFreqDay","RelatedFreqStartDD",
                          "RelatedFreqHldyStat","RelateddispMode","Related-CustTitle","Related - CustName","Related -CustAddrLine1",
                          "DocCode", "DocScanFlg", "FFD-Schmcode","AutSwpFlg","SwpDepFreqMnths","SwpFreqType","SwpHldyStat",
                          "MinBalOperAcct","RepayInstr", "ExpiryDate", "DocumentDate", 
                          "DrawingPowerInd", "SectCode", "SubSectCode", "PurpAdv", 
                          "ModeAdv", "TypeAdv", "NatAdv" ,"Relvalue","AcctID"};
            
        } else if ("Sheet2".equalsIgnoreCase(sheetName)) {
            keys = new String[] {
                "Gender", "Title", "FirstName", "LastName", "MiddleName", "ShortName", "PreferredName",
                "DateofBirth", "MinorIndicator", "Non-residentIndicator", "StaffIndicator", "PrimarySOLID", "CustomerType", "Segment",
                "Sub-segment", "ConstitutionCode" ,"TaxDeductedatSourceTable" ,"CustomerRating","Region" ,"PreferredLocale",
                "EnableCRMAlerts" ,"PrimyRelshpMangID","NativeLanguageCode" ,"PreferredNativeLanguage","preferredAddress",
                "AddressFormat","AddressType","HouseNo","StreetNo","StreetName","City","State","Country","PostalCode","AddressValidFrom"
                ,"AddressLabel","AddressLine1","SecondAddressFlg","S_preferredAddress","S_AddressFormat","S_AddressType","S_HouseNo","S_StreetNo",
                "S_StreetName","S_City","S_State","S_Country","S_PostalCode","S_AddressValidFrom","S_AddressLabel","S_AddressLine1","Delect"
                ,"AddressToDelete","P_ContactType","P_MobileAltType","P_EIDType","PhoneOrEmail","PhoneEmailType","Cntrycode","Areacode",
                "localcode","EmailId","EmailType","SecondPhoneNEnamiFlg","S_PContactType","S_PMobileAltType","S_PEIDType","S_PhoneOrEmail","S_PhoneEmailType"
                ,"S_Cntrycode","S_Areacode","S_localcode","S_EmailId","S_EmailType","PNEDelect","PNEDelectToBe","DocType","DocCode","Countryofissue","placeofissue"
                ,"UID","Issdate","ExDate","Secdocflg","S_DocType","S_DocCode","S_Countryofissue","S_placeofissue","S_UID","S_Issdate","S_ExDate"
                ,"DelectIdenty","DelectIdentyToBe","FreCode6","FreCode7","FreCode8","Nationality","MaritalStatus","occuption","crossincome"
                
            };
        } else {
            throw new IllegalArgumentException("Unsupported sheet name: " + sheetName);
        }

        for (int i = 0; i < keys.length; i++) {
            dataMap.put(keys[i], ExcelUtils2.getCellData(excelFilePath, sheetName, row, i+1));
        }
    }

    // Getter method to retrieve values dynamically
    public String getValue(String key) {
        return dataMap.getOrDefault(key, "N/A");
    }

    // Example getter methods
    public String getMenu() { return getValue("menu"); }
    public String getFunction() { return getValue("Function"); }
    public String getCifid() { return getValue("Cifid"); }
    public String getSchmccd() { return getValue("Schmccd");}
    public String getintCrActFlg() { return getValue("IntCrAcctFlg");}
    public String getnextIntCrCalcDt() { return getValue("NextIntCrCalcDt");}
    public String getrelnCode() { return getValue("RelnCode");}
    public String getdocCode() { return getValue("DocCode");}
    public String getdocScanFlg() { return getValue("DocScanFlg");}
    public String getexpiryDate() { return getValue("ExpiryDate");}
    public String getdocumentDate() { return getValue("DocumentDate");}
    public String getdrawingPowerInd() { return getValue("DrawingPowerInd");}
    public String getsectCode() { return getValue("SectCode");}
    public String getsubSectCode() { return getValue("SubSectCode");}
    public String getpurpAdv() { return getValue("PurpAdv");}
    public String getmodeAdv() { return getValue("ModeAdv");}
    public String gettypeAdv() { return getValue("TypeAdv");}
    public String getnatAdv() { return getValue("NatAdv");}
    public String getnatrelvalue() { return getValue("Relvalue");}
    public String getAcct_Statement() { return getValue("Acct_Statement");}
    public String getCalBase() { return getValue("CalBase");}
    public String getFreqType() { return getValue("FreqType");}
    public String getFreqWeek() { return getValue("FreqWeek");}
    public String getFreqDay() { return getValue("FreqDay");}
    public String getFreqStartDD() { return getValue("FreqStartDD");}
    public String getFreqHldyStat() { return getValue("FreqHldyStat");}
    public String getDespatchMode() { return getValue("DispatchMode");}
    public String getModeofoperation() { return getValue("Modeofoperation");}
    public String getminIntPcntCr() { return getValue("MinIntPcntCr");}
    public String getmaxIntPcntCr() { return getValue("MaxIntPcntCr");}
    public String getcustPrefIntCr() { return getValue("CustPrefIntCr");}
    public String getacctPrefIntCr() { return getValue("AcctPrefIntCr");}
    public String getchanPrefIntCr() { return getValue("ChanPrefIntCr");}
    public String getRegnum() { return getValue("Regnum");}
    public String getNomineeCifId() { return getValue("NomineeCifId");}
    public String getrelation() { return getValue("Relation");}
    public String getNomineePcnt() { return getValue("NomineePcnt");}
    public String getRelcode() { return getValue("Relcode");}
    public String getrelnType() { return getValue("RelnType");}
    public String getrelnCode1() { return getValue("RelnCode");}
    public String getRelatedFreqType() { return getValue("RelatedFreqType");}
    public String getRelatedFreqWeek() { return getValue("RelatedFreqWeek");}
    public String getRelatedFreqDay() { return getValue("RelatedFreqDay");}
    public String getRelatedFreqStartDD() { return getValue("RelatedFreqStartDD");}
    public String gethlyday() { return getValue("RelatedFreqHldyStat");}
    public String getRelateddespMode() { return getValue("RelateddispMode");}
    public String getRelatedCustTitle() { return getValue("Related-CustTitle");}
    public String getRelatedCustName() { return getValue("Related - CustName");}
    public String getRelatedCustAddrLine() { return getValue("Related -CustAddrLine1");}
    public String getFFDSchmcode() { return getValue("FFD-Schmcode");}
    public String getAutSwpFlg() { return getValue("AutSwpFlg");}
    public String getSwpDepFreqMnths() { return getValue("SwpDepFreqMnths");}
    public String getSwpFreqType() { return getValue("SwpFreqType");}
    public String getSwpHldyStat() { return getValue("SwpHldyStat");}
    public String getMinBalOperAcct() { return getValue("MinBalOperAcct");}
    public String getRepayInstr() { return getValue("RepayInstr");}
    public String getacctID() { return getValue("AcctID");}
    public String getSecondNomineeflg() { return getValue("SecondNomineeflg");}
    public String getSecond_NomineeCifId() { return getValue("Second_NomineeCifId");}
    public String getSecond_Relation() { return getValue("Second_Relation");}
    public String getSecond_NomineePcnt() { return getValue("Second_NomineePcnt");}
    public String getSecond_ReNo() { return getValue("Second_ReNo");}
    public String getSecond_RelatedPartyflg() { return getValue("Second_RelatedPartyflg");}
    public String getNomineeMinor() { return getValue("Nominee-Minor");}
    public String getGuardianName() { return getValue("GuardianName");}
    public String getAddressLine1() { return getValue("AddressLine1");}
    public String getAddressLine2() { return getValue("AddressLine2");}
    public String getCity() { return getValue("City");}
    public String getState() { return getValue("State");}
    public String getCountry() { return getValue("Country");}
    public String getPostalCode() { return getValue("PostalCode");}
    public String getGuardianCode() { return getValue("GuardianCode");}
    
    public String getGender() { return getValue("Gender");}
    public String getTitle() { return getValue("Title");}
    public String getFirstName() { return getValue("FirstName");}
    public String getLastName() { return getValue("LastName");}
    public String getMiddleName() { return getValue("MiddleName");}
    public String getShortName() { return getValue("ShortName");}
    public String getPreferredName() { return getValue("PreferredName");}
    public String getDateofBirth() { return getValue("DateofBirth");}
    public String getMinorIndicator() { return getValue("MinorIndicator");}
    public String getNonresidentIndicator() { return getValue("Non-residentIndicator");}
    public String getStaffIndicator() { return getValue("StaffIndicator");}
    public String getPrimarySOLID() { return getValue("PrimarySOLID");}
    public String getCustomerType() { return getValue("CustomerType");}
    public String getSegment() { return getValue("Segment");}
    public String getSubsegment() { return getValue("Sub-segment");}
    public String getConstitutionCode() { return getValue("ConstitutionCode");}
    public String getTaxDeducatSouTable() { return getValue("TaxDeductedatSourceTable");}
    public String getCustomerRating() { return getValue("CustomerRating");}
    public String getRegion() { return getValue("Region");}
    public String getPreferredLocale() { return getValue("PreferredLocale");}
    public String getEnableCRMAlerts() { return getValue("EnableCRMAlerts");}
    public String getPrimyRelshpMangID() { return getValue("PrimyRelshpMangID");}
    public String getNativeLngugeCde() { return getValue("NativeLanguageCode");}
    public String getPrfredNtieLnguge() { return getValue("PreferredNativeLanguage");}
    public String getAddressFormat() { return getValue("AddressFormat");}
    public String getpreferredAddress() { return getValue("preferredAddress");}
    public String getAddressType() { return getValue("AddressType");}
    public String getHouseNo() { return getValue("HouseNo");}
    public String getStreetNo() { return getValue("StreetNo");}
    public String getStreetName() { return getValue("StreetName");}
    public String getCity1() { return getValue("City");}
    public String getState1() { return getValue("State");}
    public String getCountry1() { return getValue("Country");}
    public String getPostalCode1() { return getValue("PostalCode");}
    public String getAddressValidFrom() { return getValue("AddressValidFrom");}
    public String getAddressLabel() { return getValue("AddressLabel");}
    public String getAddressLine11() { return getValue("AddressLine1");}
    public String getSecondAddressFlg() { return getValue("SecondAddressFlg");}
    public String getS_AddressFormat() { return getValue("S_AddressFormat");}
    public String getS_AddressType() { return getValue("S_AddressType");}
    public String getS_HouseNo() { return getValue("S_HouseNo");}
    public String getS_StreetNo() { return getValue("S_StreetNo");}
    public String getS_StreetName() { return getValue("S_StreetName");}
    public String getS_City() { return getValue("S_City");}
    public String getS_State() { return getValue("S_State");}
    public String getS_Country() { return getValue("S_Country");}
    public String getS_PostalCode() { return getValue("S_PostalCode");}
    public String getS_AddressValidFrom() { return getValue("S_AddressValidFrom");}
    public String getS_AddressLabel() { return getValue("S_AddressLabel");}
    public String getS_AddressLine1() { return getValue("S_AddressLine1");}
    public String getDelect() { return getValue("Delect");}
    public String getAddressToDelete() { return getValue("AddressToDelete");}
    public String getS_preferredAddress() { return getValue("S_preferredAddress");}
    public String getP_ContactType() { return getValue("P_ContactType");}
    public String getP_MobileAltType() { return getValue("P_MobileAltType");}
    public String getP_EIDType() { return getValue("P_EIDType");}
    public String getPhoneOrEmail() { return getValue("PhoneOrEmail");}
    public String getPhoneEmailType() { return getValue("PhoneEmailType");}
    public String getCntrycode() { return getValue("Cntrycode");}
    public String getAreacode() { return getValue("Areacode");}
    public String getlocalcode() { return getValue("localcode");}
    public String getSecondPhoneNEnamiFlg() { return getValue("SecondPhoneNEnamiFlg");}
    public String getS_PContactType() { return getValue("S_PContactType");}
    public String getS_PMobileAltType() { return getValue("S_PMobileAltType");}
    public String getS_PEIDType() { return getValue("S_PEIDType");}
    public String getEmailId() { return getValue("EmailId");}
    public String getEmailType() { return getValue("EmailType");}
    public String getS_PhoneOrEmail() { return getValue("S_PhoneOrEmail");}
    public String getS_PhoneEmailType() { return getValue("S_PhoneEmailType");}
    public String getS_Cntrycode() { return getValue("S_Cntrycode");}
    public String getS_Areacode() { return getValue("S_Areacode");}
    public String getS_localcode() { return getValue("S_localcode");}
    public String getS_EmailId() { return getValue("S_EmailId");}
    public String getS_EmailType() { return getValue("S_EmailType");}
    public String getPNEDelect() { return getValue("PNEDelect");}
    public String getPNEDelectToBe() { return getValue("PNEDelectToBe");}
    public String getDocType() { return getValue("DocType");}
    public String getDocCode() { return getValue("DocCode");}
    public String getCountryofissue() { return getValue("Countryofissue");}
    public String getplaceofissue() { return getValue("placeofissue");}
    public String getUID() { return getValue("UID");}
    public String getIssdate() { return getValue("Issdate");}
    public String getExDate() { return getValue("ExDate");}
    public String getSecdocflg() { return getValue("Secdocflg");}
    public String getS_DocType() { return getValue("S_DocType");}
    public String getS_DocCode() { return getValue("S_DocCode");}
    public String getS_Countryofissue() { return getValue("S_Countryofissue");}
    public String getS_placeofissue() { return getValue("S_placeofissue");}
    public String getS_UID() { return getValue("S_UID");}
    public String getS_Issdate() { return getValue("S_Issdate");}
    public String getS_ExDate() { return getValue("S_ExDate");}
    public String getDelectIdenty() { return getValue("DelectIdenty");}
    public String getDelectIdentyToBe() { return getValue("DelectIdentyToBe");}
    public String getFreCode6() { return getValue("FreCode6");}
    public String getFreCode7() { return getValue("FreCode7");}
    public String getFreCode8() { return getValue("FreCode8");}
    public String getNationality() { return getValue("Nationality");}
    public String getMaritalStatus() { return getValue("MaritalStatus");}
    public String getoccuption() { return getValue("occuption");}
    public String getcrossincome() { return getValue("crossincome");


    }
    public Set<String> getKeys() {
        return dataMap.keySet(); // Returns all keys stored in the dataMap
    }
    
    }


