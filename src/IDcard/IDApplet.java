package IDcard;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import org.globalplatform.GPSystem;

public class IDApplet extends Applet {
	
	private final static short MF = 0x00;
	private final static short DF = 0x01;
	private final static short EF = 0x02;
	private static short FILE_POINTER;
	private static byte FILE_NAME_FIRST;
	private static byte FILE_NAME_SECOND;

	private static final byte[] SURNAME = {'M','U','R','K'};
	private static final byte[] FIRST_NAME = {'R','O','B','I','N'};
	private static final byte[] SEX = {'M'};
	private static final byte[] CITIZEN = {'E','S','T'};
	private static final byte[] BIRTH = {50, 48, 32, 49, 50, 32, 50, 48, 48, 48, 32, 69, 83, 84};
	private static final byte[] ID_CODE = {53, 48, 48, 49, 50, 50, 48, 50, 55, 50, 50};
	private static final byte[] DOC_NUMBER = {65, 66, 48, 52, 52, 54, 54, 54, 48};
	private static final byte[] EXPIRY_DATE = {48, 54, 32, 48, 53, 32, 50, 48, 50, 53};
	private static final byte[] ISSUANCE = {48, 54, 32, 48, 53, 32, 50, 48, 50, 48};
	private static final byte[] RESIDENCE_TYPE = {0};
	private static final byte[] NOTES1 = {0};
	private static final byte[] NOTES2 = {0};
	private static final byte[] NOTES3 = {0};
	private static final byte[] NOTES4 = {0};
	private static final byte[] NOTES5 = {0};

	private static final byte[] ATRHistBytes ={
		(byte)0x00, (byte)0x12, (byte)0x23,(byte)0x3F, (byte)0x53, (byte)0x65, (byte)0x49, (byte)0x44, (byte)0x0F, (byte)0x90, (byte)0x00
	};

	private static byte[] APDULog;
	private static short logIndex;



	public static void install(byte[] ba, short offset, byte len) {
		(new IDApplet()).register();
		APDULog = new byte[256]; 
		logIndex = 0;
	}
	
	public boolean select(){
		return true;
	}

	//main method for processing APDUs
	public void process(APDU apdu) {
            
			byte[] buf = apdu.getBuffer();
			//logAPDU(buf);
			if(selectingApplet()){
				Util.arrayCopyNonAtomic(ATRHistBytes, (short)0, buf, (short)0, (short)ATRHistBytes.length);
				GPSystem.setATRHistBytes(buf, (short)0, (byte)ATRHistBytes.length);
				return;
			}
			//protocol = APDU.getProtocol();
			//protocol = protocol == APDU.PROTOCOL_T0 ? APDU.PROTOCOL_T0 : APDU.PROTOCOL_T1 ;

			//for checking main actions requested by terminal
			switch (buf[ISO7816.OFFSET_INS]) {
				case (byte)0xA4:
					selectFile(buf, apdu);
					return;
				case (byte)0xB0:
					readFile(buf, apdu);
					return;
				default:
					ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
					return;
			}
	}


	//processes file selection
	public void selectFile(byte[] buf, APDU apdu){
		switch (buf[ISO7816.OFFSET_P1]) {
			case (byte) MF:
				FILE_POINTER = MF;
				ISOException.throwIt(ISO7816.SW_NO_ERROR);
				return;


			case (byte) DF:
				if (FILE_POINTER != MF ) ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
				if(getLen(buf) == 2){
					FILE_POINTER = DF;
					//set file name
					FILE_NAME_FIRST = buf[ISO7816.OFFSET_CDATA];
					FILE_NAME_SECOND = buf[ISO7816.OFFSET_CDATA + 1];
					ISOException.throwIt(ISO7816.SW_NO_ERROR);
					return;
				}
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
				return;

			case (byte) EF:
				if (FILE_POINTER != DF && FILE_POINTER != EF) ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
				if(getLen(buf) == 2){
					FILE_POINTER = EF;
					//set file name
					FILE_NAME_FIRST = buf[ISO7816.OFFSET_CDATA];
					FILE_NAME_SECOND = buf[ISO7816.OFFSET_CDATA + 1];
					ISOException.throwIt(ISO7816.SW_NO_ERROR);
					return;
				}
				return;
			default:
				ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
				return;
		}
	}



	public void readFile(byte[] buf, APDU apdu){
		if (FILE_NAME_FIRST != (byte)0x50 || FILE_POINTER != EF) {
			ISOException.throwIt(ISO7816.SW_FILE_INVALID);
		}
		
		switch (FILE_NAME_SECOND) {
			case (byte)0x01:
				sendPD(SURNAME, buf, apdu);
				return;
			case (byte)0x02:
				sendPD(FIRST_NAME, buf, apdu);
				return;
			case (byte)0x03:
				sendPD(SEX, buf, apdu);
				return;
			case (byte)0x04:
				sendPD(CITIZEN, buf, apdu);
				return;
			case (byte)0x05:
				sendPD(BIRTH, buf, apdu);
				return;
			case (byte)0x06:
				sendPD(ID_CODE, buf, apdu);
				return;
			case (byte)0x07:
				sendPD(DOC_NUMBER, buf, apdu);
				return;
			case (byte)0x08:
				sendPD(EXPIRY_DATE, buf, apdu);
				return;
			case (byte)0x09:
				sendPD(ISSUANCE, buf, apdu);
				return;
			case (byte)0x0a:
				sendPD(RESIDENCE_TYPE, buf, apdu);
				return;
			case (byte)0x0b:
				sendPD(NOTES1, buf, apdu);
				return;
			case (byte)0x0c:
				sendPD(NOTES2, buf, apdu);
				return;
			case (byte)0x0d:
				sendPD(NOTES3, buf, apdu);
				return;
			case (byte)0x0e:
				sendPD(NOTES4, buf, apdu);
				return;
			case (byte)0x0f:
				sendPD(NOTES5, buf, apdu);
				return;
			default:
			byte[] data = {FILE_NAME_FIRST,FILE_NAME_SECOND};
			sendPD(data, buf, apdu);
			return;
				//ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
		}
	}

	public short getLen(byte[] buf){
		return (short) (buf[ISO7816.OFFSET_LC] & (short)0xff);
	}
	
	public void sendPD(byte[] DATA, byte[] buf, APDU apdu){
		Util.arrayCopyNonAtomic(DATA, (short)0, buf, (short)0, (short)DATA.length);
		apdu.setOutgoingAndSend((short)0, (short)DATA.length);
	}

	public void logAPDU(byte[] buf){
		Util.arrayCopyNonAtomic(buf, (short)0, APDULog, logIndex, (short)buf.length);
		logIndex += (short)buf.length;
		APDULog[logIndex] = 127; //do distinguish between commands
		logIndex++;
	}

}

