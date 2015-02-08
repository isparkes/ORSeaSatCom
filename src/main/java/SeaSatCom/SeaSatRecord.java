/* ====================================================================
 * Limited Evaluation License:
 *
 * The exclusive owner of this work is Tiger Shore Management Ltd.
 * This work, including all associated documents and components
 * is Copyright Tiger Shore Management Ltd 2006-2010.
 *
 * The following restrictions apply unless they are expressly relaxed in a
 * contractual agreement between the license holder or one of its officially
 * assigned agents and you or your organisation:
 *
 * 1) This work may not be disclosed, either in full or in part, in any form
 *    electronic or physical, to any third party. This includes both in the
 *    form of source code and compiled modules.
 * 2) This work contains trade secrets in the form of architecture, algorithms
 *    methods and technologies. These trade secrets may not be disclosed to
 *    third parties in any form, either directly or in summary or paraphrased
 *    form, nor may these trade secrets be used to construct products of a
 *    similar or competing nature either by you or third parties.
 * 3) This work may not be included in full or in part in any application.
 * 4) You may not remove or alter any proprietary legends or notices contained
 *    in or on this work.
 * 5) This software may not be reverse-engineered or otherwise decompiled, if
 *    you received this work in a compiled form.
 * 6) This work is licensed, not sold. Possession of this software does not
 *    imply or grant any right to you.
 * 7) You agree to disclose any changes to this work to the copyright holder
 *    and that the copyright holder may include any such changes at its own
 *    discretion into the work
 * 8) You agree not to derive other works from the trade secrets in this work,
 *    and that any such derivation may make you liable to pay damages to the
 *    copyright holder
 * 9) You agree to use this software exclusively for evaluation purposes, and
 *    that you shall not use this software to derive commercial profit or
 *    support your business or personal activities.
 *
 * This software is provided "as is" and any expressed or impled warranties,
 * including, but not limited to, the impled warranties of merchantability
 * and fitness for a particular purpose are disclaimed. In no event shall
 * Tiger Shore Management or its officially assigned agents be liable to any
 * direct, indirect, incidental, special, exemplary, or consequential damages
 * (including but not limited to, procurement of substitute goods or services;
 * Loss of use, data, or profits; or any business interruption) however caused
 * and on theory of liability, whether in contract, strict liability, or tort
 * (including negligence or otherwise) arising in any way out of the use of
 * this software, even if advised of the possibility of such damage.
 * This software contains portions by The Apache Software Foundation, Robert
 * Half International.
 * ====================================================================
 */
package SeaSatCom;

import OpenRate.record.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A Record corresponds to a unit of work that is being processed by the
 * pipeline. Records are created in the InputAdapter, pass through the Pipeline,
 * and written out in the OutputAdapter. Any stage of the pipeline my update the
 * record in any way, provided that later stages in the processing and the
 * output adapter know how to treat the record they receive.
 *
 * As an alternative, you may define a less flexible record format as you wish
 * and fill in the fields as required, but this costs performance.
 *
 * Generally, the record should know how to handle the following operations by
 * linking the appropriate method:
 *
 * mapOriginalData() [mandatory] ----------------- Transformation from a flat
 * record as read by the input adapter to a formatted record.
 *
 * unmapOriginalData() [mandatory if you wish to write output files]
 * ------------------- Transformation from a formatted record to a flat record
 * ready for output.
 *
 * getDumpInfo() [optional] ------------- Preparation of the dump equivalent of
 * the formatted record, ready for dumping out to a dump file.
 *
 * In this simple example, we require only to read the "B-Number", and write the
 * "Destination" as a result of this. Because of the simplicity of the example
 * we do not perform a full mapping, we just handle the fields we want directly,
 * which is one of the advantages of the BBPA model (map as much as you want or
 * as little as you have to).
 *
 */
public class SeaSatRecord extends RatingRecord {

  /**
   * CVS version info - Automatically captured and written to the Framework
   * Version Audit log at Framework startup. For more information please
   * <a target='new' href='http://www.open-rate.com/wiki/index.php?title=Framework_Version_Map'>click
   * here</a> to go to wiki page.
   */
  public static String CVS_MODULE_INFO = "OpenRate, $RCSfile: SeaSatRecord.java,v $, $Revision: 1.26 $, $Date: 2012-07-22 09:55:23 $";

  // Used to manage recycles
  public static final String RECYCLE_TAG = "ORRECYCLE";

  // Field Splitter in the records
  public static final String FIELD_SPLITTER = ";";

  // These are the mappings to the fields for GSM service
  public static final int IDX_GSM_USAGE_PERIOD = 0;
  public static final int IDX_GSM_BILLING_PERIOD = 1;
  public static final int IDX_GSM_BILL_DATE = 2;
  public static final int IDX_GSM_BILL_NUMBER = 3;
  public static final int IDX_GSM_BILL_RECIPIENT = 4;
  public static final int IDX_GSM_NUMBER_RECIPIENT = 5;
  public static final int IDX_GSM_FISCAL_CODE = 6;
  public static final int IDX_GSM_PRICE_LIST_CODE = 7;
  public static final int IDX_GSM_A_NUMBER = 8;
  public static final int IDX_GSM_A_CUSTOMER = 9;
  public static final int IDX_GSM_CONTRACT_NUMBER = 10;
  public static final int IDX_GSM_BILLING_TYPE = 11;
  public static final int IDX_GSM_CALL_TYPE = 12;
  public static final int IDX_GSM_CALL_ORIGIN = 13;
  public static final int IDX_GSM_CALL_DESTINATION = 14;
  public static final int IDX_GSM_B_NUMBER = 15;
  public static final int IDX_GSM_CALL_DATE = 16;
  public static final int IDX_GSM_CALL_TIME = 17;
  public static final int IDX_GSM_ORIG_CHARGED_QUANTITY = 18;
  public static final int IDX_GSM_BILL_CHARGED_QUANTITY = 19;
  public static final int IDX_GSM_CHARGED_AMOUNT_UOM = 20;
  public static final int IDX_GSM_CHARGED_AMOUNT = 21;
  public static final int GSM_FIELD_COUNT = 22;

  //These are the mappings to the fields for the Satellite service
  public static final int IDX_SAT_TERMINAL = 0;
  public static final int IDX_SAT_A_NUMBER = 1;
  public static final int IDX_SAT_DATE = 2;
  public static final int IDX_SAT_TIME = 3;
  public static final int IDX_SAT_LES = 4;
  public static final int IDX_SAT_SATELLITE = 5;
  public static final int IDX_SAT_DEST_NAME = 6;
  public static final int IDX_SAT_NUMBER_DIALLED = 7;
  public static final int IDX_SAT_SERVICE_NAME = 8;
  public static final int IDX_SAT_DURATION = 9;
  public static final int IDX_SAT_AMOUNT = 10;
  public static final int IDX_SAT_DEST_CODE = 11;
  public static final int IDX_SAT_SERVICE_CODE = 12;
  public static final int SAT_FIELD_COUNT = 13;

  // The record type is what allows us to determine what the records to handle
  // are, and what to ignore. Generally you will need something of this type
  public static final int GSM_RECORD_TYPE = 20;
  public static final int SAT_RECORD_TYPE = 30;

  // Worker variables to save references during processing. We are using the
  // B-Number to look up the destination.
  public String B_Number = null;
  public String Norm_B_Number = null;
  public long longQuantity = 0;
  public double Quantity = 0;
  public double RatedAmount = 0;
  public String StreamName = null;
  public String Destination = null;
  public String Origin = null;
  public String UOM = null;
  public String Direction = null;
  public boolean PassportCall = false; // used to detect passport calls
  public String PriceSheet = null;    // used to locate the price sheet to use
  public String ServiceDescription;
  public String ProductService;       // Used to locate the products for the service
  public boolean International = false;// Used to perform the category identification
  public boolean SpecialNumber = false;// Used to perform the category identification
  public String JBCategory;           // Used to group into orders in Jbilling
  public String JBDescription;        // Line item description in Jbilling

  // Internal Management Fields
  public String GuidingKey = null;
  public String CustIDA = null;
  public int BalanceGroup = 0;
  public String UsedProduct = null;
  public String Description = null;
  public String CDRType = null;
  public String OriginZone;
  public boolean ConnectionFee = false;

  // Customer management variables
  public String SubscriptionID = null;
  //public String  BillingMonth = "200806";
  //public int     BillingCycleID;
  public String OriginDescription;
  public String DestinationZone;
  public String DestinationDescription;
  public int JBUserId;
  public boolean UserTariffOverridden;
  public String OriginalUserTariff;
  public String UserTariff;
  public ArrayList<String> Options = new ArrayList<>();
  public String UsedDiscount = "";
  public String DiscountOption = "";        // used in pack pricing
  public int Recycle_Count;

  public Date BillingCycleStart;          // The start of the billing cycle
  public Date BillingCycleEnd;            // The end of the billing cycle
  public double DiscountGranted;            // Amount discounted
  public String DiscountRUM = "";           // The discounted RUM
  public String DiscountRule = "";          // Rules applied to this CDR
  public int PackUsageFlag = 0;          // Used for tracking pack usage
  public long RecId;

  /**
   * Default Constructor for RateRecord, creating the empty record container
   */
  public SeaSatRecord() {
    super();
  }

  /**
   * Utility function to map a main record for the GSM Input file type.
   *
   * Call Case ---------
   *
   * We determine the call case use the origin, destination and B-Number fields.
   * The rules are:
   *
   * If the B-Number is "-" the call is a termination call If the ORIGIN field
   * is "-", the call was made in France If the DESTINATION field is "-", the
   * call is a FRA-FRA call
   *
   * @param OriginalData The original data to map
   */
  public void mapGSMRecord(String OriginalData) {
    RecordError tmpError;
    String tmpDate;
    String tmpDurBits[];
    String tmpDuration;

    SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    sdfInput.setLenient(false);

    // GSM record
    this.RECORD_TYPE = SeaSatRecord.GSM_RECORD_TYPE;
    this.OriginalData = OriginalData;

    // Detect recycle case
    if (this.OriginalData.startsWith(RECYCLE_TAG)) {
      StringBuffer record = new StringBuffer(this.OriginalData);

      // remove RecycleTag from record
      record = record.delete(0, record.indexOf(";") + 1);

      // remove ErrorCode from record
      record = record.delete(0, record.indexOf(";") + 1);

      // Get the previous recycle count
      String Recycle_CountStr = record.substring(0, record.indexOf(";"));
      Recycle_Count = Integer.parseInt(Recycle_CountStr);

      // remove RecycleCount from record
      record = record.delete(0, record.indexOf(";") + 1);

      // reset the original data
      this.OriginalData = record.toString();
    }

    this.fields = this.OriginalData.split(";");

    // Check we have the right number of fields
    if (fields.length != GSM_FIELD_COUNT) {
      tmpError = new RecordError("ERR_INPUT_RECORD_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
      return;
    }

    // get the Unit of Measure
    UOM = stripQuoteChars(getField(IDX_GSM_CHARGED_AMOUNT_UOM));

    // Pull out the B-Number and make it easy to access for the lookup. Note
    // that we don't have to do this, we could just as easily leave it where
    // it is and extract it at the time of the lookup.
    B_Number = stripQuoteChars(getField(IDX_GSM_B_NUMBER));

    // remove the Xs, and replace with 0s so we can do zoning
    B_Number = B_Number.replaceAll("x", "0");

    // Set the guiding key and A Number
    GuidingKey = stripQuoteChars(getField(IDX_GSM_A_NUMBER));
    BalanceGroup = Integer.parseInt(GuidingKey);
    Destination = stripQuoteChars(getField(IDX_GSM_CALL_DESTINATION));
    Origin = stripQuoteChars(getField(IDX_GSM_CALL_ORIGIN));
    Service = "GSM";

    // get the billing cycle, used for controlling the counter periods
    String[] BillingPeriod = stripQuoteChars(getField(IDX_GSM_USAGE_PERIOD)).split(" - ");

    if (BillingPeriod.length != 2) {
      tmpError = new RecordError("ERR_BILLING_PERIOD_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
      return;
    }

    try {
      tmpDate = BillingPeriod[0] + " 00:00:00";
      BillingCycleStart = sdfInput.parse(tmpDate);
    } catch (ParseException ex) {
      tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
    }

    try {
      tmpDate = BillingPeriod[1] + " 23:59:59";
      BillingCycleEnd = sdfInput.parse(tmpDate);
    } catch (ParseException ex) {
      tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
    }

    // Get the CDR date, and the UTC version of it
    try {
      tmpDate = stripQuoteChars(getField(IDX_GSM_CALL_DATE)) + " " + stripQuoteChars(getField(IDX_GSM_CALL_TIME));
      tmpDate = tmpDate.replace("h", ":");
      EventStartDate = sdfInput.parse(tmpDate);
      UTCEventDate = EventStartDate.getTime() / 1000;
    } catch (ParseException ex) {
      tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
    }

    switch (UOM) {
      case "message":
        // SMS
        Direction = "Outgoing";
        CDRType = "SMMO";
        ServiceDescription = "SMS";
        ProductService = "SMS_MMS";
        // Parse the event count
        try {
          Quantity = Double.parseDouble(stripQuoteChars(getField(IDX_GSM_BILL_CHARGED_QUANTITY)));
        } catch (NumberFormatException nfe) {
          Quantity = 0;
          tmpError = new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION);
          this.addError(tmpError);
        } // deal with the MMS types
        if (getField(IDX_GSM_CALL_TYPE).contains("MMS Photo")) {
          ServiceDescription = "MMS Photo";
        } else if (getField(IDX_GSM_CALL_TYPE).contains("MMS Texte")) {
          ServiceDescription = "MMS Texte";
        } else if (getField(IDX_GSM_CALL_TYPE).contains("MMS Video")) {
          ServiceDescription = "MMS Video";
        } // if it is an MMS to an email, set the Norm b number to national
        if (getField(IDX_GSM_CALL_TYPE).contains("adresse email")) {
          B_Number = "0033";
        }
        setRUMValue("EVT", Quantity);
        break;
      case "S":
        // Voice
        if (B_Number.equals("-")) {
          Direction = "Incoming";
          CDRType = "MTC";
          Norm_B_Number = "00";
        } else {
          Direction = "Outgoing";
          CDRType = "MOC";
        }
        ServiceDescription = "Voice Call";
        ProductService = "VOICE";
        tmpDuration = stripQuoteChars(getField(IDX_GSM_BILL_CHARGED_QUANTITY));
        // To be decommissioned now that "appel" has been introduced
        if (tmpDuration.equals("-")) {
          //unknown duration
          Quantity = 0;

          // set the connection fee
          setRUMValue("CONNFEE", 1);

          // Set the duration value
          setRUMValue("DUR", Quantity);

          // Set the flag for the price sheet - we only have connection fees for
          // outgoing calls
          if (Direction.equals("Outgoing")) {
            ConnectionFee = true;
          }
        } else {
          tmpDurBits = tmpDuration.split(":");

          Quantity = Integer.parseInt(tmpDurBits[0]) * 3600
                  + Integer.parseInt(tmpDurBits[1]) * 60
                  + Integer.parseInt(tmpDurBits[2]);

          // Set the duration value
          setRUMValue("DUR", Quantity);
        }
        break;
      case "appel":
        // Voice
        if (B_Number.equals("-")) {
          Direction = "Incoming";
          CDRType = "MTC";
          Norm_B_Number = "00";
        } else {
          Direction = "Outgoing";
          CDRType = "MOC";
        }
        ServiceDescription = "Voice Call";
        ProductService = "VOICE";
        // set the connection fee
        setRUMValue("CONNFEE", 1);
        // Set the duration value
        setRUMValue("DUR", 0);
        // Set the flag for the price sheet - we only have connection fees for
        // outgoing calls
        if (Direction.equals("Outgoing")) {
          ConnectionFee = true;
        }
        break;
      case "Mo":
        // data session
        Direction = "Outgoing";
        CDRType = "SCDR";
        ServiceDescription = "Data";
        ProductService = "DATA";
        tmpDuration = stripQuoteChars(getField(IDX_GSM_BILL_CHARGED_QUANTITY));
        tmpDuration = tmpDuration.replace(",", ".");
        try {
          Quantity = Double.parseDouble(tmpDuration);
        } catch (NumberFormatException nfe) {
          Quantity = 0;
          tmpError = new RecordError("ERR_VOLUME_INVALID", ErrorType.DATA_VALIDATION);
          this.addError(tmpError);
        }
        setRUMValue("VOL", Quantity);
        break;
      case "unité":
        // synchronisation service
        Direction = "Outgoing";
        CDRType = "SCDR";
        ServiceDescription = "Service";
        ProductService = "Service";
        // Parse the event count
        try {
          Quantity = Double.parseDouble(stripQuoteChars(getField(IDX_GSM_BILL_CHARGED_QUANTITY)));
        } catch (NumberFormatException nfe) {
          Quantity = 0;
          tmpError = new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION);
          this.addError(tmpError);
        }
        setRUMValue("EVT", Quantity);
        break;
      case "session":
        // WAP session
        Direction = "Outgoing";
        CDRType = "SCDR";
        ServiceDescription = "WAP Session";
        ProductService = "Service";
        setRUMValue("EVT", 1);
        break;
      case "connexion":
        // WAP session
        Direction = "Outgoing";
        CDRType = "SCDR";
        ServiceDescription = "ServiceNK";
        ProductService = "ServiceNK";
        setRUMValue("EVT", 1);
        break;
      default:
        tmpError = new RecordError("ERR_UNKNOWN_UOM", ErrorType.DATA_VALIDATION);
        this.addError(tmpError);
        break;
    }

    // get the passport flag
    this.PassportCall = getField(IDX_GSM_CALL_TYPE).contains("Passport");

    // Get the description
    this.Description = getField(IDX_GSM_CALL_TYPE);

    // Get the price sheet for this call
    if (Origin.equals("-")) {
      if (ConnectionFee) {
        PriceSheet = "National " + ServiceDescription + " " + Direction + " Connection";
      } else {
        PriceSheet = "National " + ServiceDescription + " " + Direction;
      }
    } else {
      // Set the flag for the call category determination
      International = true;

      if (ConnectionFee) {
        PriceSheet = "International " + ServiceDescription + " " + Direction + " Connection";
      } else {
        PriceSheet = "International " + ServiceDescription + " " + Direction;
      }
    }

    // Create a charge packet
    ChargePacket tmpCP = new ChargePacket();
    tmpCP.service = Service;
    tmpCP.packetType = "R";
    tmpCP.ratePlanName = "P.1";
    tmpCP.zoneModel = CDRType;
    tmpCP.timeModel = "FLAT";
    this.addChargePacket(tmpCP);
  }

  /**
   * Utility function to map a main record for the SAT Input file type
   *
   * @param OriginalData The original data to map
   */
  public void mapSATRecord(String OriginalData) {
    RecordError tmpError;
    String tmpDate;
    String tmpDuration;
    String tmpOrigData;

    SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    sdfInput.setLenient(false);

    // Satellite record
    this.RECORD_TYPE = SeaSatRecord.SAT_RECORD_TYPE;
    this.OriginalData = OriginalData;

    // Detect recycle case
    if (this.OriginalData.startsWith(RECYCLE_TAG)) {
      StringBuffer record = new StringBuffer(this.OriginalData);

      // remove RecycleTag from record
      record = record.delete(0, record.indexOf(";") + 1);

      // remove ErrorCode from record
      record = record.delete(0, record.indexOf(";") + 1);

      // Get the previous recycle count
      String Recycle_CountStr = record.substring(0, record.indexOf(";"));
      Recycle_Count = Integer.parseInt(Recycle_CountStr);

      // remove RecycleCount from record
      record = record.delete(0, record.indexOf(";") + 1);

      // reset the original data
      this.OriginalData = record.toString();
    }

    // parse the strange data format
    tmpOrigData = OriginalData.replaceAll("\",\"", ";");
    tmpOrigData = tmpOrigData.replaceAll("\",", ";");
    tmpOrigData = tmpOrigData.replaceAll(",\"", ";");
    tmpOrigData = tmpOrigData.replaceAll("\"", "");
    tmpOrigData = tmpOrigData.replaceAll(",", ".");
    this.fields = tmpOrigData.split(";");

    // Check we have the right number of fields
    if (fields.length != SAT_FIELD_COUNT) {
      tmpError = new RecordError("ERR_INPUT_RECORD_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
      return;
    }

    // Pull out the B-Number and make it easy to access for the lookup. Note
    // that we don't have to do this, we could just as easily leave it where
    // it is and extract it at the time of the lookup.
    B_Number = stripQuoteChars(getField(IDX_SAT_NUMBER_DIALLED));

    // Set the guiding key and A Number
    GuidingKey = stripQuoteChars(getField(IDX_SAT_TERMINAL));
    Destination = stripQuoteChars(getField(IDX_SAT_DEST_CODE));
    Origin = "SAT";
    Service = "SAT";
    OriginZone = "Satellite";
    OriginDescription = "Satellite";

    // Get the CDR date, and the UTC version of it
    try {
      tmpDate = stripQuoteChars(getField(IDX_SAT_DATE)) + " " + stripQuoteChars(getField(IDX_SAT_TIME));
      EventStartDate = sdfInput.parse(tmpDate);
      UTCEventDate = EventStartDate.getTime() / 1000;

      // Set the default end date - we need to have this set for the
      // time lookup
      EventEndDate = sdfInput.parse(tmpDate);
    } catch (ParseException ex) {
      tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
      this.addError(tmpError);
    }

    switch (stripQuoteChars(getField(IDX_SAT_SERVICE_NAME))) {
      case "VOIX":
        // Satellite voice
        Direction = "Outgoing";
        CDRType = "SMOC";
        UOM = "S";
        PriceSheet = "Sat Voice";
        ProductService = "VOICE";
        break;
      case "FAX":
        // Satellite fax
        Direction = "Outgoing";
        CDRType = "SFAX";
        UOM = "S";
        PriceSheet = "Sat Fax";
        ProductService = "VOICE";
        break;
      case "HSD64":
        // High speed circuit switched data??
        Direction = "Outgoing";
        CDRType = "SHSD";
        UOM = "S";
        PriceSheet = "Sat HSD64";
        ProductService = "DATA";
        break;
      case "IP-DATA":
        // Packet switched data??

        Direction = "Outgoing";
        CDRType = "SSCDR";
        if (stripQuoteChars(getField(IDX_SAT_NUMBER_DIALLED)).equals("MPDS")) {
          UOM = "bit";
          PriceSheet = "Sat MPDS";
        } else {
          UOM = "byte";
          PriceSheet = "Sat Data";
        }
        ProductService = "DATA";
        break;
      case "SMS":
        // Satellite SMS
        Direction = "Outgoing";
        CDRType = "SSMS";
        UOM = "SMS";
        PriceSheet = "Sat SMS";
        ProductService = "SSMS";
        break;
      default:
        // Unknown service
        tmpError = new RecordError("ERR_UNKNOWN_SERVICE", ErrorType.DATA_VALIDATION);
        this.addError(tmpError);
        return;
    }

    switch (UOM) {
      case "S":
        // Parse the duration
        try {
          Quantity = Double.parseDouble(stripQuoteChars(getField(IDX_SAT_DURATION)));
          longQuantity = Long.parseLong(stripQuoteChars(getField(IDX_SAT_DURATION)));
        } catch (NumberFormatException nfe) {
          Quantity = 0;
          tmpError = new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION);
          this.addError(tmpError);
          return;
        } // Adjust the end date - required by time splitting
        EventEndDate.setTime(UTCEventDate + longQuantity * 1000);
        setRUMValue("DUR", Quantity);
        break;
      case "bit":
        tmpDuration = stripQuoteChars(getField(IDX_SAT_DURATION));
        tmpDuration = tmpDuration.replace(",", ".");
        try {
          Quantity = Double.parseDouble(tmpDuration);
        } catch (NumberFormatException nfe) {
          Quantity = 0;
          tmpError = new RecordError("ERR_VOLUME_INVALID", ErrorType.DATA_VALIDATION);
          this.addError(tmpError);
          return;
        } // convert from bits to MB
        Quantity /= (1024 * 1024 * 8);
        setRUMValue("VOL", Quantity);
        break;
      case "byte":
        tmpDuration = stripQuoteChars(getField(IDX_SAT_DURATION));
        tmpDuration = tmpDuration.replace(",", ".");
        try {
          Quantity = Double.parseDouble(tmpDuration);
        } catch (NumberFormatException nfe) {
          Quantity = 0;
          tmpError = new RecordError("ERR_VOLUME_INVALID", ErrorType.DATA_VALIDATION);
          this.addError(tmpError);
          return;
        } // convert from bytes to MB
        Quantity /= (1024 * 1024);
        setRUMValue("VOL", Quantity);
        break;
      case "SMS":
        Quantity = 1;
        setRUMValue("EVT", Quantity);
        break;
      default:
        tmpError = new RecordError("ERR_UNKNOWN_UOM", ErrorType.DATA_VALIDATION);
        this.addError(tmpError);
        return;
    }

    this.Description = getField(IDX_SAT_DEST_NAME);

    // Create a charge packet
    ChargePacket tmpCP = new ChargePacket();
    tmpCP.service = Service;
    tmpCP.packetType = "R";
    tmpCP.ratePlanName = "";
    tmpCP.zoneModel = Service;
    tmpCP.timeSplitting = 0;
    this.addChargePacket(tmpCP);
  }

  /**
   * Maps the call category based on the discount, passport and call case for
   * GSM Records. This is used to select the order in JBilling to associate this
   * record with.
   */
  public void mapJBCategoryGSM() {
    boolean Discounted = UsedDiscount.length() > 0;

    // set the default
    JBCategory = "Misc";

    // ************************** International ********************************
    if (International) {
      if (Direction.equals("Outgoing")) {
        switch (ProductService) {
          case "VOICE":
            if (PassportCall) {
              JBCategory = "Appel Voix International émis (Vodafone)";
            } else {
              JBCategory = "Appel Voix International émis (Monde+)";
            }
            break;
          case "SMS_MMS":
            JBCategory = "SMS International émis";
            break;
          case "DATA":
            if (UserTariff.startsWith("BMail")) {
              JBCategory = "Session Bmail International";
            } else if (UserTariff.startsWith("M2M")) {
              JBCategory = "Session M2M International";
            } else if (UserTariff.equals("Default")) {
              JBCategory = "Session Données International";
            } else {
              JBCategory = "Session Web 3G International";
            }
            break;
        }
      } else {
        if (ProductService.equals("VOICE")) {
          if (PassportCall) {
            JBCategory = "Appel Voix International reçu (Vodafone)";
          } else {
            JBCategory = "Appel Voix International reçu (Monde+)";
          }
        }
      }
    } // ***************************** National **********************************
    else {
      switch (ProductService) {
        case "VOICE":
          if (Discounted) {
            JBCategory = "Appel Voix National";
          } else {
            JBCategory = "Appel Voix National émis hors forfait";
          }
          break;
        case "SMS_MMS":
          if (Discounted) {
            JBCategory = "SMS National inclus";
          } else {
            JBCategory = "SMS National émis hors forfait";
          }
          break;
        case "DATA":
          if (UserTariff.startsWith("BMail")) {
            if (Discounted) {
              JBCategory = "Session Bmail National inclus";
            } else {
              JBCategory = "Session Bmail National hors forfait";
            }
          } else if (UserTariff.startsWith("M2M")) {
            JBCategory = "Session M2M National";
          } else if (UserTariff.equals("Default")) {
            JBCategory = "Session Données National";
          } else {
            JBCategory = "Session Web 3G National";
          }
          break;
      }
    }

    // Special numbers are special - they override everything
    if (SpecialNumber) {
      JBCategory = "Appel vers numéros spéciaux";
    }
  }

  /**
   * Maps the call description based on the direction for GSM Records. This is
   * used to select the order line in JBilling to associate this record with.
   */
  public void mapJBDescriptionGSM() {
    if (Direction.equals("Outgoing")) {
      if (ServiceDescription.equals("ServiceNK")) {
        JBDescription = "Misc";
      } else {
        JBDescription = ServiceDescription + " (" + OriginDescription + " - " + DestinationDescription + ")";
      }
    } else {
      JBDescription = ServiceDescription + " (" + OriginDescription + ")";
    }
  }

  /**
   * Maps the call category based on the discount, passport and call case for
   * SAT Records. This is used to select the order in JBilling to associate this
   * record with.
   */
  public void mapJBCategorySAT() {
    // set the default
    JBCategory = "Misc";

    switch (ProductService) {
      case "VOICE":
        if (CDRType.equals("SFAX")) {
          JBCategory = "Appel Fax";
        } else {
          JBCategory = "Appel Voix Satellite émis";
        }
        break;
      case "DATA":
        JBCategory = "Session Data Satellite";
        break;
      case "SSMS":
        JBCategory = "Session SMS";
        break;
      default:
        JBCategory = "Unknown";
        break;
    }

  }

  /**
   * Maps the call description based on the direction for SAT Records. This is
   * used to select the order line in JBilling to associate this record with.
   */
  public void mapJBDescriptionSAT() {
    JBDescription = Description;
  }

  /**
   * Reconstruct the record from the field values, replacing the original
   * structure of tab separated records
   *
   * @return The unmapped data
   */
  public String unmapOriginalData() {

    int NumberOfFields;
    int i;
    StringBuffer tmpReassemble;

    if (RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {

      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      tmpReassemble = new StringBuffer(1024);

      NumberOfFields = this.fields.length;

      for (i = 0; i < NumberOfFields; i++) {

        if (i == 0) {
          tmpReassemble.append(stripQuoteChars(this.fields[i]));
        } else {
          tmpReassemble.append(FIELD_SPLITTER);
          tmpReassemble.append(stripQuoteChars(this.fields[i]));
        }
      }

      // return the re-assembled string
      return tmpReassemble.toString();
    }

    return this.OriginalData;
  }

  public String stripQuoteChars(String input) {
    return input.replaceAll("\"", "");
  }

  /**
   * Reconstruct the record from the field values, replacing the original
   * structure of tab separated records
   *
   * @return The unmapped data
   */
  public String unmapJBillingGSMData() {
    StringBuffer recordData;

    if (RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {

      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      recordData = new StringBuffer(1024);

      recordData.append(this.JBUserId);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.Service);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.CDRType);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.JBCategory);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.JBDescription);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.PriceSheet);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.OriginZone);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DestinationZone);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.UserTariff);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.Quantity);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.RatedAmount);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.UsedDiscount);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountGranted);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountRule);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountRUM);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.CounterCycle);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.RecId);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.PackUsageFlag);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.unmapOriginalData());

      // return the re-assembled string
      return recordData.toString();
    }

    return unmapOriginalData();
  }

  /**
   * Reconstruct the record from the field values, replacing the original
   * structure of tab separated records
   *
   * @return The unmapped data
   */
  public String unmapJBillingSATData() {
    StringBuffer recordData;

    if (RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE) {

      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      recordData = new StringBuffer(1024);

      recordData.append(this.JBUserId);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.Service);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.CDRType);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.JBCategory);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.JBDescription);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.PriceSheet);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.OriginZone);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DestinationZone);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.UserTariff);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.Quantity);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.RatedAmount);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.UsedDiscount);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountGranted);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountRule);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountRUM);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.CounterCycle);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.RecId);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.PackUsageFlag);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.unmapOriginalData());

      // return the re-assembled string
      return recordData.toString();
    }

    return unmapOriginalData();
  }

  /**
   * Reconstruct the record from the field values, replacing the original
   * structure of tab separated records
   *
   * @return The unmapped data
   */
  public String unmapJBillingPackData() {
    StringBuffer recordData;
    double PackRatedAmount = 0;
    String JBPackDesc = this.DiscountOption + " pack jour " + this.getField(IDX_GSM_CALL_DATE);

    if (RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {
      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      recordData = new StringBuffer(1024);

      recordData.append(this.JBUserId);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.Service);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.CDRType);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append("Pack Usage");  // JB Category
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(JBPackDesc); // JB Description
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountOption);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.OriginZone);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DestinationZone);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.UserTariff);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(1);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);

      // find the pack price
      for (int CPi = 0; CPi < this.getChargePacketCount(); CPi++) {
        ChargePacket tmpCP = this.getChargePacket(CPi);

        if (tmpCP.packetType.equals("P")) {
          PackRatedAmount = tmpCP.chargedValue;
        }
      }
      recordData.append(PackRatedAmount);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.UsedDiscount);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);

      // we don't want to report the discount granted twice, so blank it
      recordData.append(0);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountRule);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.DiscountRUM);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.CounterCycle);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.RecId);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(0);
      recordData.append(SeaSatRecord.FIELD_SPLITTER);
      recordData.append(this.unmapOriginalData());

      // return the re-assembled string
      return recordData.toString();
    }

    return unmapOriginalData();
  }

  @Override
  public ArrayList<String> getDumpInfo() {
    BalanceImpact tmpBalImp;
    int tmpBalImpCount;
    int tmpErrorCount;
    int i;
    int j;
    RecordError tmpError;
    ArrayList<String> tmpDumpList;
    tmpDumpList = new ArrayList<>();
    int tmpChargePacketCount;
    ChargePacket tmpCP;

    if (this.RECORD_TYPE == 10) {
      // Format the fields
      tmpDumpList.add("============ HEADER RECORD ============");
      tmpDumpList.add("  Record Number   = <" + this.RecordNumber + ">");
      tmpDumpList.add("  original record = <" + this.OriginalData + ">");
    }
    int OptionCount;

    if ((this.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE)
            | (this.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
      // Format the fields
      tmpDumpList.add("============ DETAIL RECORD ============");
      tmpDumpList.add("  Record Number   = <" + this.RecordNumber + ">");
      tmpDumpList.add("  Technology      = <" + this.Service + ">");
      tmpDumpList.add("  B Number        = <" + this.B_Number + ">");
      tmpDumpList.add("  Customer        = <" + this.getField(IDX_GSM_A_CUSTOMER) + ">");
      tmpDumpList.add("  Norm B Number   = <" + this.Norm_B_Number + ">");
      tmpDumpList.add("  CDR Start Date  = <" + this.EventStartDate + ">");
      tmpDumpList.add("  Original UOM    = <" + this.UOM + ">");
      tmpDumpList.add("  Rateable Qty    = <" + this.Quantity + ">");
      tmpDumpList.add("  Origin          = <" + this.Origin + ">");
      tmpDumpList.add("  Destination     = <" + this.Destination + ">");
      tmpDumpList.add("  Direction       = <" + this.Direction + ">");
      tmpDumpList.add("  CDR Type        = <" + this.CDRType + ">");
      tmpDumpList.add("  Passport        = <" + this.PassportCall + ">");
      tmpDumpList.add("  Description     = <" + this.Description + ">");
      tmpDumpList.add("       --- Customer Attributes ---");
      tmpDumpList.add("  Guiding Key     = <" + this.GuidingKey + ">");
      tmpDumpList.add("  Jbilling CustID = <" + this.JBUserId + ">");
      tmpDumpList.add("  Jbilling Cat    = <" + this.JBCategory + ">");
      tmpDumpList.add("  Jbilling Desc   = <" + this.JBDescription + ">");
      tmpDumpList.add("  Balance Group   = <" + this.BalanceGroup + ">");
      tmpDumpList.add("       --- Rating Attributes ---");
      tmpDumpList.add("  CDR UTC Date    = <" + this.UTCEventDate + ">");
      tmpDumpList.add("  Service         = <" + this.ProductService + ">");
      tmpDumpList.add("  Tariff          = <" + this.UserTariff + ">");
      tmpDumpList.add("  Discount        = <" + this.UsedDiscount + ">");
      tmpDumpList.add("  Orig Tariff     = <" + this.OriginalUserTariff + ">");
      tmpDumpList.add("  Price sheet     = <" + this.PriceSheet + ">");
      tmpDumpList.add("  Origin Zone     = <" + this.OriginZone + ">");
      tmpDumpList.add("  Origin Desc     = <" + this.OriginDescription + ">");
      tmpDumpList.add("  Dest Zone       = <" + this.DestinationZone + ">");
      tmpDumpList.add("  Dest Desc       = <" + this.DestinationDescription + ">");
      tmpDumpList.add("  Pack Usage      = <" + this.PackUsageFlag + ">");
      tmpDumpList.add("  Rated Amount    = <" + this.RatedAmount + ">");
      tmpDumpList.add("  Discount Amount = <" + this.DiscountGranted + ">");
      tmpDumpList.add("  Discount Used   = <" + this.UsedDiscount + ">");
      tmpDumpList.add("  Discount Rule   = <" + this.DiscountRule + ">");
      tmpDumpList.add("  Discount RUM    = <" + this.DiscountRUM + ">");
      tmpDumpList.add("  Counter Cycle   = <" + this.CounterCycle + ">");
      tmpDumpList.add("  Counter Rec Id  = <" + this.RecId + ">");
      tmpDumpList.add("  Output Stream   = <" + this.getOutputs().toString() + ">");
      tmpDumpList.add("  Recycle count   = <" + this.Recycle_Count + ">");

      OptionCount = this.Options.size();
      if (OptionCount > 0) {
        tmpDumpList.add("---------------- Options ------------------");

        for (i = 0; i < OptionCount; i++) {
          tmpDumpList.add("    Option          = <" + Options.get(i) + ">");
        }
      }

      // Add the charge packets
      tmpDumpList.addAll(this.getChargePacketsDump());

      // Add any balance impacts
      tmpDumpList.addAll(this.getBalanceImpactsDump());
    }

    if (this.RECORD_TYPE == 90) {
      // Format the fields
      tmpDumpList.add("============ TRAILER RECORD ============");
      tmpDumpList.add("  Record Number   = <" + this.RecordNumber + ">");
      tmpDumpList.add("  original record = <" + this.OriginalData + ">");
    }

    tmpErrorCount = this.getErrors().size();
    tmpDumpList.add("  Errors          = <" + tmpErrorCount + ">");
    if (tmpErrorCount > 0) {
      tmpDumpList.add("-------------- ERRORS ----------------");
      for (i = 0; i < tmpErrorCount; i++) {
        tmpError = (RecordError) this.getErrors().get(i);
        tmpDumpList.add("    Error           = <" + tmpError.getMessage() + ">");
      }
    }

    return tmpDumpList;
  }
}
