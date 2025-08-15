package entra;

//for svg processing
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

//for general image processing
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

//mouse events
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;
import javax.swing.JPanel;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import java.io.ByteArrayInputStream;

import java.io.IOException;

import java.text.NumberFormat;
import java.time.OffsetDateTime;
//import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Random;

import com.microsoft.graph.beta.models.*;
//import com.microsoft.graph.beta.models.ExternalAuthenticationType;
//import com.microsoft.graph.beta.models.odataerrors.*;
import com.microsoft.graph.beta.models.odataerrors.ODataError;
import com.microsoft.graph.core.CoreConstants.OdataInstanceAnnotations;

//import io.jsonwebtoken.security.Password;

public class MFAExtras {

    //when checking phonetypes so we don't have to specify the whole path every time.
    final static AuthenticationPhoneType phoneTypeMobile = 
        com.microsoft.graph.beta.models.AuthenticationPhoneType.Mobile;
    final static AuthenticationPhoneType phoneTypeAltMobile = 
        com.microsoft.graph.beta.models.AuthenticationPhoneType.AlternateMobile;
    final static AuthenticationPhoneType phoneTypeOffice =
        com.microsoft.graph.beta.models.AuthenticationPhoneType.Office;

    //possible string values for user preferred secondary authentication
    final static String defaultSms = "sms";
    final static String defaultPush = "push";
    final static String defaultOath = "oath";
    final static String defaultVoiceMobile = "voiceMobile";
    final static String defaultVoiceOffice = "voiceOffice";
    final static String defaultVoiceAltMobile = "voiceAlternateMobile";

    static public enum PhoneType {
        OFFICE("office"),
        MOBILE("mobile"),
        ALTMOBILE("altMobile");

        private final String description;

        //Constructor
        PhoneType(String description) {
            this.description = description;
        }

        //Getter
        public String getDescription() {
            return description;
        }
    } 

    static public enum MethodType {
        EMAIL,
        PHONE
    } 

    //set strings
        static final String messageStdCodeExists = "You have an existing Standard QR Code method";
        static final String messageStdCodeNew = "You need to create a new Standard QR Code method";
        static final String messageTmpCodeNoStd = "You must configure a standard code before you can configure a temporary one.\n";
        static final String messagePinNoStd = "You must configure a standard code in order to view/reset PIN information.\n" +
            "If this user has a standard QR code, then it needs to be deleted so you can create a new PIN.";
        //static final String windowTitle = "QR Code Authentication";
        static final String labelLifeTime = "LifeTime in hours   : ";
        static final String labelLastUsed = "Last Used DateTime  : ";
        static final String labelCreated = "Created DateTime    : ";
        static final String labelActive = "Active DateTime     : ";
        static final String labelExpires = "Expiration DateTime : ";
        static final String labelUpdated = "Updated DateTime    : ";
        static final String labelId = "Id : ";
        static final String labelStdDisplayTitle = "Standard QR Code Details";
        static final String labelTmpDisplayTitle = "Temporary QR Code Details";
        static final String labelPinDisplayTitle = "PIN Information";
        static final String labelStdCreateTitle = "Standard QR Code Creation Options";
        static final String labelTmpCreateTitle = "Temporary QR Code Creation Options";
        static final String labelActivationDate = "Set Activation Date : "; 
        static final String labelSetExp = "Set Expiration Date : ";
        static final String labelActivateLater = "Activate Later";
        static final String labelForceChange = "Force Change on next signin:";
        static final String labelName = "activateLaterLabel";
        static final String nameStdCodePane = "StdPane";
        static final String nameTmpCodePane = "TmpPane";
        static final String namePinPane = "PINPane";
        static final String namePinInput = "PinInput";
        static final String nameAddMfaInputText = "inputText";
        static final String nameAddMfaRadioPhoneButton = "radioPhoneButton";
        static final String nameAddMfaRadioMobileButton = "radioMobilePhoneButton";
        static final String nameAddMfaRadioAltMobileButton = "radioAltMobilePhoneButton";
        static final String nameAddMfaRadioOfficeButton = "radioOfficeButton";
        static final String nameAddMfaRadioEmailButton = "radioEmailButton";
        static final String nameActivateLaterCheck = "ActivateLaterCheck";
        static final String StdCodePane = "Standard QR Code";
        static final String TmpCodePane = "Temporary QR Code";
        static final String PinCodePane = "PIN Details";
        static final String labelEnterPin = "Enter PIN";
        static final String dateSpinnerAct = "dateSpinnerAct";
        static final String dateSpinnerExp = "dateSpinnerExp";
        static final String spinnerTmpLife = "TmpLifetimeSpinner";
        static final String buttonDelStd = "Delete this Standard QR Code";
        static final String buttonDelTmp = "Delete this Temporary QR Code";
        static final String buttonCreateStd = "Create a Standard QR Code";
        static final String buttonCreateTmp = "Create a Temporary QR Code";
        static final String buttonChangeExp = "Change Expiration Date";
        static final String buttonResetPin = "Reset PIN";
        static final String labelNoPinExplanation = "This user has a PIN configured. You can RESET it if needed.";
        static final String labelNewPin = "New PIN : ";
        static final String stringDefaultMethod = " **Default Method**\n";
        static final String stringPinToolTip = "Only viewable with a new standard QR Code, or reset the previous PIN.";
        static final String stringPinEntryToolTip = "Leave blank to generate a temporary PIN.";

        static final Integer stdMinLifeTime = 1;
        static final Integer stdMaxLifeTime = 395;
        static final Integer tmpLifeMin = 1;
        static final Integer tmpLifeMax = 12;
        static final Integer tmpLifeDefault = 3;
        static final Integer tmpLifeStep = 1;

        static final Insets insetsButton = new Insets(0,50,0,50);
        static final Insets insetZero = new Insets(0,0,0,0);
    
    // values for possible phone types
    final static String phoneMobile = "mobile";
    final static String phoneAltMobile = "alternateMobile";
    final static String phoneOffice = "office";
    //authentication method ID for password
    final static String passwordId = "28c10230-6103-485e-b985-444c60001490";

    //object used to hold the data for the TAP creation window
    public static class tapData {       
        public OffsetDateTime startDateTime;
        public int lifetimeInMins;
        public Boolean isUsableOnce;
        public int defaultLifetimeInMins;
        public int minLifetimeInMins;
        public int maxLifetimeInMins;
    }

    // Check if the tenant is premium
    public static Boolean isTenantPremium() {
        Boolean successful = false;

        try {
            // Get org info
            var response = App.graphClient.organization().get();

            var plans = response.getValue().getFirst().getAssignedPlans();

            for (var plan : plans) {
                // Check if the plan is AAD Premium
                //App.outputArea.append("Checking plan: " + plan.getService() + "\n");
                if (plan.getService().equalsIgnoreCase("AADPremiumService")
                    && plan.getCapabilityStatus().equalsIgnoreCase("Enabled")) {
                    successful = true;
                    break;
                }
            }
        } 
        catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error getting Org info. Pleaes try again.\n" + ex.getMessage());
        }
        // Return the result
        return successful;
    }
    
    //Check if the user is a member of a list of groups, if so, return the Id
    public static String checkGroupMembership(List<ExcludeTarget> targets) {
        //initialize the string to return, default to "None"
        String groupId = "None";

        for (ExcludeTarget group : targets) {
            JOptionPane.showMessageDialog(null, "Checking : " + group.getId());
            if (isMemberOfGroup(group.getId())) {
                
                groupId = group.getId();
                //we got a group and are successful, we can break the loop
                break;
            }
        }

        //return the groupId
        return groupId;
    }

    // Check if user is a member of the passed group
    public static Boolean isMemberOfGroup(String groupId) {
        //initialize var to return and default to false
        Boolean isMember = false;

        try {
            //get the list of transitive groups - direct & indirect membership, as well as admin units
            DirectoryObjectCollectionResponse response = 
                App.graphClient.users().byUserId(App.activeUser.getId()).transitiveMemberOf().get();
            //loop through groups and check if one matches the group - we only need one
            for (DirectoryObject item : response.getValue()) {
                if (item.getId().equalsIgnoreCase(groupId)) {
                    isMember = true;
                    break;
                }
            }
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error checking group membership: " + ex.getMessage());
        }
        return isMember;
    }

    // get the list of authentication methods for a user
    public static List<AuthenticationMethod> getUserMfaMethods() {
        //initialize the list we will return
        List<AuthenticationMethod> result = new ArrayList<>();

        try {
            //send the request
            var response = App.graphClient.users().byUserId(App.activeUser.getId())
                .authentication().methods().get();
            result = response.getValue();
        }
        catch (ODataError ex) {
            JOptionPane.showMessageDialog(null, "Error getting MFA methods" + ex.getMessage(), null, 0);
        }
        //return the result
        return result;
    }

    // get the default method for MFA from user sign-in preferences
    public static String getDefaultMethod() {
        //initialize the variable we will return.
        String defaultMethod = "NULL";

        try {
            //send the request for the user's sign-in preferences
            var result = App.graphClient.users().byUserId(App.activeUser.getId())
                .authentication().signInPreferences().get();
            //if its not null, get the value and assign it
            if ( null != result.getUserPreferredMethodForSecondaryAuthentication())
                defaultMethod = result.getUserPreferredMethodForSecondaryAuthentication().value;
        }
        catch (ODataError ex) {
            JOptionPane.showMessageDialog(null, "Error getting sign-in preferences:\n" + ex.getMessage(), null, 0);
        }
        //return the result
        return defaultMethod;
    }

    // get the tenant registration report information
    // this is dependent on the tenant being premium and my not work in all tenants
    public static void getRegistrationAuthData () {
        //initialize the string to hold the output
        StringBuilder message = new StringBuilder();
        UserRegistrationDetails response = null;
        
        if (!isTenantPremium()) {
            App.outputArea.append("Tenant is not premium, cannot get registration data.\n");
            App.outputArea.append("Please contact your admin to enable premium features.\n\n");
        } else {
            try {
                //get the registration report
                response = App.graphClient.reports().authenticationMethods().userRegistrationDetails()
                    .byUserRegistrationDetailsId(App.activeUser.getId()).get();

                //if we have a response, loop through it and print out the details
                if (null != response.getId()) {
                    message.append("\n*****************User Registration Data*****************\n");
                    message.append("\nUser is an admin in Entra ID :   ").append(response.getIsAdmin());
                    message.append("\nUser is MFA registered       :   ").append(response.getIsMfaRegistered());
                    message.append("\nUser is MFA capable          :   ").append(response.getIsMfaCapable());
                    message.append("\nUser is SSPR registered      :   ").append(response.getIsSsprRegistered());
                    message.append("\nUser is SSPR enabled         :   ").append(response.getIsSsprEnabled());
                    message.append("\nUser is SSPR capable         :   ").append(response.getIsSsprCapable());
                    message.append("\nUser is passwordless capable :   ").append(response.getIsPasswordlessCapable());
                }
                else {
                    message.append("No registration data found for user: ").append(App.activeUser.getDisplayName()).append("\n");
                }
            } catch (ODataError ex) {
                JOptionPane.showMessageDialog(null, "Error getting registration auth methods:\n" + ex.getMessage(), null, 0);
            }
        }

        //print the output to the output area
        App.outputArea.append(message.toString());
    }
    //get the MFA methods and print them out in a readable way
    // also identify the default method in the output
    //  possible defaults are only SMS, MSAuth, oath, voice mobile, voice office,
    //      and voice alt mobile 
    public static void getAndPrintUserMFA() {
        {
            // Get and print the tenant registration data, if available.
            getRegistrationAuthData();

            // Get the user preferred default method
            String defaultMethod = getDefaultMethod();

            try {
                // Get authentication methods for the active user
                AuthenticationMethodCollectionResponse methodsResponse = App.graphClient
                        .users().byUserId(App.activeUser.getId())
                        .authentication().methods().get();
                
                // Create string to hold the output
                StringBuilder message = new StringBuilder("\nAuthentication Methods for " + App.activeUser.getDisplayName() + ":\n");
                message.append("Default method: ").append(defaultMethod).append("\n");
                // Loop through the authentication methods and print out details depending on the type
                for (AuthenticationMethod method : methodsResponse.getValue()) {
                    switch (method)
                    {
                        case PlatformCredentialAuthenticationMethod platformMethod:
                            message.append("\nPlatform Credential:\n");
                            message.append("  ID                : ").append(platformMethod.getId()).append("\n");
                            message.append("  Display Name      : ").append(platformMethod.getDisplayName()).append("\n");
                            message.append("  Created DateTime  : ").append(platformMethod.getCreatedDateTime()).append("\n");
                            break;
                        case WindowsHelloForBusinessAuthenticationMethod whfbMethod:
                            message.append("\nWindows Hello for Business:\n");
                            message.append("  ID                : ").append(whfbMethod.getId()).append("\n");
                            message.append("  Display Name      : ").append(whfbMethod.getDisplayName()).append("\n");
                            message.append("  Device ID         : ").append(whfbMethod.getDevice()).append("\n");
                            message.append("  Created DateTime  : ").append(whfbMethod.getCreatedDateTime()).append("\n");
                            break;
                        case TemporaryAccessPassAuthenticationMethod tapMethod:
                            message.append("\nTemporary Access Pass:\n");
                            message.append("  ID                : ").append(tapMethod.getId()).append("\n");
                            message.append("  Is Usable Once    : ").append(tapMethod.getIsUsableOnce()).append("\n");
                            message.append("  Start DateTime    : ").append(tapMethod.getStartDateTime()).append("\n");
                            message.append("  Lifetime in Mins  : ").append(tapMethod.getLifetimeInMinutes()).append("\n");
                            message.append("  Created DateTime  : ").append(tapMethod.getCreatedDateTime()).append("\n");
                            break;
                        case SoftwareOathAuthenticationMethod oathMethod:
                            message.append("\nSoftware OATH: ");
                            if ( defaultOath == defaultMethod)
                                message.append(" **Usable as Default Method**\n");
                            else
                                message.append("\n");
                            message.append("  ID                : ").append(oathMethod.getId()).append("\n");
                            message.append("  Display Name      : ").append(oathMethod.getId()).append("\n");
                            message.append("  Secret Key        : ").append(oathMethod.getSecretKey()).append("\n");
                            message.append("  Created DateTime  : ").append(oathMethod.getCreatedDateTime()).append("\n");
                            break;
                        case MicrosoftAuthenticatorAuthenticationMethod authMethod:
                            message.append("\nMicrosoft Authenticator: ");
                            if ( defaultPush == defaultMethod)
                                message.append(stringDefaultMethod);
                            else
                                message.append("\n");
                            message.append("  ID                : ").append(authMethod.getId()).append("\n");
                            message.append("  Display Name      : ").append(authMethod.getDisplayName()).append("\n");
                            message.append("  Phone App Version : ").append(authMethod.getPhoneAppVersion()).append("\n");
                            message.append("  Device Tag        : ").append(authMethod.getDeviceTag()).append("\n");
                            message.append("  Created DateTime  : ").append(authMethod.getCreatedDateTime()).append("\n");
                            break;
                        case PhoneAuthenticationMethod phoneMethod:
                            message.append("\nPhone Authentication: ");
                            if (defaultSms == defaultMethod && phoneTypeMobile == phoneMethod.getPhoneType())
                                message.append(stringDefaultMethod);
                            else if (defaultVoiceMobile == defaultMethod && phoneTypeMobile == phoneMethod.getPhoneType()) 
                                message.append(stringDefaultMethod);
                            else if (defaultVoiceAltMobile == defaultMethod && phoneTypeAltMobile == phoneMethod.getPhoneType()) 
                                message.append(stringDefaultMethod);  
                            else if (defaultVoiceOffice == defaultMethod && phoneTypeOffice == phoneMethod.getPhoneType()) 
                                message.append(stringDefaultMethod); 
                            else
                                message.append("\n");
                            message.append("  ID: ").append(phoneMethod.getId()).append("\n");
                            message.append("  Phone Number      : ").append(phoneMethod.getPhoneNumber()).append("\n");
                            message.append("  Phone Type        : ").append(phoneMethod.getPhoneType()).append("\n");
                            if (phoneMethod.getPhoneType() == phoneTypeMobile
                                || phoneMethod.getPhoneType() == phoneTypeAltMobile)
                                message.append("  SMS Sign-In State : ").append(phoneMethod.getSmsSignInState()).append("\n");
                            message.append("  Created DateTime  : ").append(phoneMethod.getCreatedDateTime()).append("\n");
                            break;
                        case PasswordAuthenticationMethod passwordMethod:
                            message.append("\nPassword Method :\n");
                            message.append("  Id                : ").append(passwordMethod.getId()).append("\n");
                            message.append("  Created DateTime  : ").append("\n");
                            break;
                        case EmailAuthenticationMethod emailMethod:
                            message.append("\nEmail Method   :\n");
                            message.append("  Id                : ").append(emailMethod.getId()).append("\n");
                            message.append("  Email Address     : ").append(emailMethod.getEmailAddress()).append("\n");
                            message.append("  Created DateTime  : ").append(emailMethod.getCreatedDateTime()).append("\n");
                            break;
                        case HardwareOathAuthenticationMethod hardOathMethod:
                            message.append("\nHardware OATH: ");
                            if ( defaultPush == defaultMethod)
                                message.append(" **Useable as Default Method**\n");
                            else
                                message.append("\n");
                            message.append("  ID               : ").append(hardOathMethod.getId()).append("\n");
                            message.append("  Device           : ").append(hardOathMethod.getDevice()).append("\n");
                            for ( Map.Entry<String, Object> data : hardOathMethod.getAdditionalData().entrySet()) {
                                message.append(data.getKey()).append(" : ").append(data.getValue()).append("\n");
                            }
                            message.append("  AdditionalData   : ").append(hardOathMethod.getAdditionalData()).append("\n");
                            message.append("  Created DateTime : ").append(hardOathMethod.getCreatedDateTime()).append("\n");
                            break;
                        case Fido2AuthenticationMethod fido2AuthMethod:
                            message.append("\nFIDO2 Method: \n");
                            message.append("  ID               : ").append(fido2AuthMethod.getId()).append("\n");
                            message.append("  Display Name     : ").append(fido2AuthMethod.getDisplayName()).append("\n");
                            message.append("  Created DateTime : ").append(fido2AuthMethod.getCreatedDateTime()).append("\n");
                            break;
                        
                        case QrCodePinAuthenticationMethod qrCodePinMethod:
                            message.append("\nQR Code PIN Method: " + qrCodePinMethod.getId() + "\n");
                            
                            if (null != qrCodePinMethod.getStandardQRCode()) {
                                QrCode stdMethod = qrCodePinMethod.getStandardQRCode();
                                message.append(" Standard QR Code\n");
                                message.append("  ID                : ").append(stdMethod.getId()).append("\n");
                                message.append("  Created DateTime  : ").append(stdMethod.getCreatedDateTime()).append("\n");
                                message.append("  Start DateTime    : ").append(stdMethod.getStartDateTime()).append("\n");
                                message.append("  Expires DateTime  : ").append(stdMethod.getExpireDateTime()).append("\n");
                                message.append("  LastUsed DateTime : ").append(stdMethod.getLastUsedDateTime()).append("\n");
                            }
                            if (null != qrCodePinMethod.getTemporaryQRCode()) {
                                QrCode tmpMethod = qrCodePinMethod.getTemporaryQRCode();
                                message.append(" Temporary QR Code\n");
                                message.append("  ID                : ").append(tmpMethod.getId()).append("\n");
                                message.append("  Created DateTime  : ").append(tmpMethod.getCreatedDateTime()).append("\n");
                                message.append("  Start DateTime    : ").append(tmpMethod.getStartDateTime()).append("\n");
                                message.append("  Expires DateTime  : ").append(tmpMethod.getExpireDateTime()).append("\n");
                                message.append("  LastUsed DateTime : ").append(tmpMethod.getLastUsedDateTime()).append("\n");
                            }
                            if (null != qrCodePinMethod.getPin()) {
                                QrPin qrPin = qrCodePinMethod.getPin();
                                message.append(" QR Code PIN\n");
                                message.append("  ID                : ").append(qrPin.getId()).append("\n");
                                message.append("  Created DateTime  : ").append(qrPin.getCreatedDateTime()).append("\n");
                                message.append("  Updated DateTime  : ").append(qrPin.getUpdatedDateTime()).append("\n");
                                message.append("  ForceChange       : ").append(qrPin.getForceChangePinNextSignIn()).append("\n");
                            }
                            break;
                        // this list is all inclusive, so we should not get here
                        default:
                            message.append("\nOther Method Type : ").append(method.getOdataType());
                            message.append("  ID                : ").append(method.getId()).append("\n");
                            message.append("  Created DateTime  : ").append(method.getCreatedDateTime()).append("\n");
                            break;
                    }
                    // Add more else-if blocks for other method types as needed
                    message.append("\n");
                }

                App.outputArea.append(message.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error getting authentication methods: " + ex.getMessage());
            }
        }
    }

    // delete an MFA method
    public static Boolean deleteMethod(String id){
        Boolean successful = false;
        //Boolean print = false;
        String error = "Error deleting method : ";
        String errorTitle = "Error removing method";
        String messagePasswordError = "Unable to delete passwords.";

        var MFAList = getUserMfaMethods();

        for (AuthenticationMethod method : MFAList) {
            if (method instanceof QrCodePinAuthenticationMethod qrCode && !method.getId().equals(id)) {
                if(null != qrCode.getStandardQRCode()){
                    if(qrCode.getStandardQRCode().getId().equals(id)) {
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .qrCodePinMethod().standardQRCode().delete();
                        } catch (ODataError e) {
                            JOptionPane.showMessageDialog(null, error + id + "\n" + e.getMessage(), errorTitle, 0);
                        }
                    } 
                }
                if(null != qrCode.getTemporaryQRCode()) {
                    if (qrCode.getTemporaryQRCode().getId().equals(id)) {
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .qrCodePinMethod().temporaryQRCode().delete();
                        } catch (ODataError e) {
                            JOptionPane.showMessageDialog(null, error + id + "\n" + e.getMessage(), errorTitle, 0);
                        }
                    }
                }
                if (null != qrCode.getPin()) {
                    if (qrCode.getPin().getId().equals(id)) {
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .qrCodePinMethod().pin().delete();
                        } catch (ODataError e) {
                            JOptionPane.showMessageDialog(null, error + id + "\n" + e.getMessage(), errorTitle, 0);
                        }
                    }
                } 
                }
            
            if (method.getId().equalsIgnoreCase(id)) {
                switch (method)
                {
                    case PlatformCredentialAuthenticationMethod platformMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .platformCredentialMethods().byPlatformCredentialAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case WindowsHelloForBusinessAuthenticationMethod whfbMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .windowsHelloForBusinessMethods().byWindowsHelloForBusinessAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case TemporaryAccessPassAuthenticationMethod tapMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .temporaryAccessPassMethods().byTemporaryAccessPassAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case SoftwareOathAuthenticationMethod oathMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .softwareOathMethods().bySoftwareOathAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case MicrosoftAuthenticatorAuthenticationMethod authMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .microsoftAuthenticatorMethods().byMicrosoftAuthenticatorAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case PhoneAuthenticationMethod phoneMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .phoneMethods().byPhoneAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case PasswordAuthenticationMethod passwordMethod:
                        JOptionPane.showMessageDialog(null, messagePasswordError, errorTitle, 0);
                        break;
                    case EmailAuthenticationMethod emailMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .emailMethods().byEmailAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case HardwareOathAuthenticationMethod hardOathMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .hardwareOathMethods().byHardwareOathAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case Fido2AuthenticationMethod fido2AuthMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .fido2Methods().byFido2AuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    case QrCodePinAuthenticationMethod qrCodePinMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .qrCodePinMethod().delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;
                    /*case ExternalAuthenticationMethod externalAuthMethod:
                        try {
                            App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                                .externalMethods().byExternalAuthenticationMethodId(id).delete();
                            successful = true;
                        }
                        catch (ODataError ex) {
                            JOptionPane.showMessageDialog(null, error + id, errorTitle, 0);
                        }
                        break;*/
                    default:
                        JOptionPane.showMessageDialog(null, "Unable to locate by the given ID.", errorTitle, 0);
                        break;
                }
            }
        }

        return successful;
    }

    //Return a string for the type of auth method
    public static String getMethodName (AuthenticationMethod method) {
        String methodType = "";

        switch (method)
            {
                case PlatformCredentialAuthenticationMethod platformMethod:
                    methodType = "Platform Credential";
                    break;
                case WindowsHelloForBusinessAuthenticationMethod whfbMethod:
                    methodType = "Windows Hello For Business";
                    break;
                case TemporaryAccessPassAuthenticationMethod tapMethod:
                    methodType = "Temporary Access Pass";
                    break;
                case SoftwareOathAuthenticationMethod oathMethod:
                    methodType = "Software Oath Method";
                    break;
                case MicrosoftAuthenticatorAuthenticationMethod authMethod:
                    methodType = "Microsoft Authenticator";
                    break;
                case PhoneAuthenticationMethod phoneMethod:
                    methodType = "Phone";
                    break;
                case PasswordAuthenticationMethod passwordMethod:
                    methodType = "Password";
                    break;
                case EmailAuthenticationMethod emailMethod:
                    methodType = "Email";
                    break;
                case HardwareOathAuthenticationMethod hardOathMethod:
                    methodType = "Hardware Oath";
                    break;
                default:
                    break;
        }

        return methodType;
    }

    //get the authentication method given by Id 
    public static AuthenticationMethod getAuthenticationMethod(String id) {
        AuthenticationMethod result = null;
        List<AuthenticationMethod> methods = getUserMfaMethods();
        for (AuthenticationMethod method : methods) {
            if (method.getId() != null && method.getId().equals(id)) {
                result = method;
            }
        }
        return result;
    }

    // Create the TAP window
    public static JFrame createTAPWindow(tapData tapData) {
        //initialize a new JFrame and the pane
        JFrame tapWindow = new JFrame();
        Container pane = tapWindow.getContentPane();
        
        //set strings
        String futureBoxText = "Check to box to specify a creation time.";
        String dateTimeText = "Please select the start time for the TAP";
        String lifeTimeText = "Please set the lifetime duration in minutes:";
        String usableOnceText = "Would you like the user to use the TAP only once?";

        //Configure the new window
        tapWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tapWindow.setSize(500,300);
        tapWindow.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        //create variables for the needed components
        JButton button;
        JCheckBox checkBox;
        JLabel label;
        //JTextComponent text;

        label = new JLabel(futureBoxText);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(label, c);

        checkBox = new JCheckBox();
        checkBox.setName("futureCheckBox");
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 50;
        c.gridx = 2;
        c.gridy = 0;
        checkBox.addActionListener(e -> checkBoxStartTimeListener(tapWindow));
        pane.add(checkBox, c);

        label = new JLabel(dateTimeText);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        pane.add(label, c);

        //date time picker.
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MM-dd-yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setName("dateSpinner");
        dateSpinner.setEnabled(false);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 1;
        pane.add(dateSpinner, c);

        label = new JLabel(lifeTimeText);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(label, c);

        SpinnerModel model = new SpinnerNumberModel(tapData.defaultLifetimeInMins, tapData.minLifetimeInMins,
            tapData.maxLifetimeInMins, 1);     
        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0");
        spinner.setEditor(editor);
        spinner.setName("lifetimeSpinner");
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 2;
        pane.add(spinner, c);

        label = new JLabel(usableOnceText);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(label, c);

        checkBox = new JCheckBox();
        checkBox.setName("usableOnceCheckBox");
        checkBox.setToolTipText("If disabled, one-time-use is required by your settings.");
        if (tapData.isUsableOnce) {
            checkBox.setSelected(true);
            checkBox.setEnabled(false);
        }
        else
            checkBox.setSelected(false);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 3;
        pane.add(checkBox, c);

        button = new JButton("Create TAP");
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 4;
        button.addActionListener(e -> generateTAPRequest_Click(tapData, tapWindow));
        pane.add(button, c);

        tapWindow.setVisible(true);
        return tapWindow;
    }

    public static void checkBoxStartTimeListener(JFrame frame) {
        Component[] comps = frame.getContentPane().getComponents();
        
        for (Component comp : comps) {
            if (comp.getName() != null && comp.getName().equals("dateSpinner")) {
                //JOptionPane.showMessageDialog(null, comp.getName());
                comp.setEnabled(!comp.isEnabled());
                break;
            }
        }
    }

    public static void generateTAPRequest_Click(tapData tapData, JFrame frame) {
        //get the components from the frame
        Component[] comps = frame.getContentPane().getComponents();
        OffsetDateTime startDateTime = null;
        int lifetimeInMins = 0;
        Boolean isUsableOnce = false;

        for (Component comp : comps) {
            if (comp instanceof JSpinner spinner) {
                if (spinner.getName().equals("lifetimeSpinner")) {
                    lifetimeInMins = (Integer) spinner.getValue();
                    App.outputArea.append("Lifetime in minutes: " + lifetimeInMins + "\n");
                } else if (spinner.getName().equals("dateSpinner")) {
                    Date startTime = (Date) spinner.getValue();
                    startDateTime = OffsetDateTime.ofInstant(startTime.toInstant(), java.time.ZoneId.systemDefault());
                }
            } else if (comp instanceof JCheckBox checkBox) {
                if (checkBox.isSelected()) {
                    isUsableOnce = true;
                }
            }
        }

        //set the data in the tapData object
        tapData.startDateTime = startDateTime;
        tapData.lifetimeInMins = lifetimeInMins;
        tapData.isUsableOnce = isUsableOnce;

        //4. Generate the request from the input
        TemporaryAccessPassAuthenticationMethod requestBody = new TemporaryAccessPassAuthenticationMethod();
        TemporaryAccessPassAuthenticationMethod result = null;

        requestBody.setStartDateTime(tapData.startDateTime.withOffsetSameInstant(ZoneOffset.UTC));
        requestBody.setLifetimeInMinutes(tapData.lifetimeInMins);
        requestBody.setIsUsableOnce(tapData.isUsableOnce);

        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //5. Submit the request
        try {
            result = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                .temporaryAccessPassMethods().post(requestBody);
        } catch (ODataError ex) {
            JOptionPane.showMessageDialog(null, "Error creating TAP method.\n" + ex.getMessage(),
                "Error creating TAP", JOptionPane.ERROR_MESSAGE);
        }

        //6. Display the result
        if (null != result) {
            String message = "Temporary Access Pass created successfully!\n" +
                "Temporary Access Pass: " + result.getTemporaryAccessPass() + "\n" +
                "Temporary Access Pass Start DateTime: " + result.getStartDateTime() + "\n" +
                "Temporary Access Pass Lifetime in Minutes: " + result.getLifetimeInMinutes() + "\n";

            JOptionPane.showMessageDialog(frame, message, "TAP Created", JOptionPane.INFORMATION_MESSAGE);
        } else 
            JOptionPane.showMessageDialog(frame, "Unable to read result.");

        //close the window
        frame.dispose();
    }

    public static JFrame createAddMethodWindow() {
        //initialize a new JFrame and the pane
        JFrame mfaWindow = new JFrame();
        Container pane = mfaWindow.getContentPane();
        
        //set strings
        String labelTypeOfAuth = "Please select the type of authentication method to add.";
        String labelUsageExample = "Please enter the number as +1 1234567890";
        String labelPhone = "Phone";
        String labelEmail = "Email";
        String labelOffice = "Office";
        String labelMobile = "Mobile";
        String labelAltMobile = "Alternate Mobile";
        String title = "Add Authentication Method";
        String nameAddMfaUsageExample = "usageExample";

        //Configure the new window
        mfaWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mfaWindow.setSize(600,250);
        mfaWindow.setLayout(new GridBagLayout());
        mfaWindow.setTitle(title);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        //create variables for the needed components
        JButton button;
        JRadioButtonMenuItem radioButton;
        JLabel label;
        ButtonGroup methodTypeGroup = new ButtonGroup();
        ButtonGroup phoneTypeGroup = new ButtonGroup();
        //JTextComponent text;

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the source of the event
                JRadioButtonMenuItem source = (JRadioButtonMenuItem) e.getSource();
                // Check which radio button was selected
                if (source.getName().equals(nameAddMfaRadioMobileButton)) {
                    phoneTypeGroup.clearSelection();
                    source.setSelected(true);

                    // Show the phone type options
                    Component[] components = pane.getComponents();
                    for (Component comp : components) {
                        if (null == comp.getName()) {
                            continue; // Skip components without a name
                        } else if (comp.getName().equals(nameAddMfaRadioMobileButton)) {
                            comp.setVisible(true);
                        } else if (comp.getName().equals(nameAddMfaRadioAltMobileButton)) {
                            comp.setVisible(true);
                        } else if (comp.getName().equals(nameAddMfaRadioOfficeButton)) {
                            comp.setVisible(true);
                        } else if (comp.getName().equals(nameAddMfaUsageExample)) {
                            comp.setVisible(true);
                        }
                    }   
                } else if (source.getName().equals(nameAddMfaRadioEmailButton)) {
                    phoneTypeGroup.clearSelection();
                    source.setSelected(true);

                    Component[] components = pane.getComponents();
                    for (Component comp : components) {
                        if (null == comp.getName()) {
                            continue; // Skip components without a name
                        } else if (comp.getName().equals(nameAddMfaRadioMobileButton)) {
                            comp.setVisible(false);
                        } else if (comp.getName().equals(nameAddMfaRadioAltMobileButton)) {
                            comp.setVisible(false);
                        } else if (comp.getName().equals(nameAddMfaRadioOfficeButton)) {
                            comp.setVisible(false);
                        } else if (comp.getName().equals(nameAddMfaUsageExample)) {
                            comp.setVisible(false);
                        }
                    } 
                }
            }
        };
        
        label = new JLabel(labelTypeOfAuth);
        c.weightx = 0.0;
        c.gridwidth = 3;
        c.ipadx = 10;
        c.ipady = 10;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(label, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName(nameAddMfaRadioPhoneButton);
        radioButton.setText(labelPhone);
        methodTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 20;
        c.ipady = 0;
        c.gridx = 0;
        c.gridy = 1;
        radioButton.addActionListener(listener);
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName(nameAddMfaRadioEmailButton);
        radioButton.setText(labelEmail);
        methodTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 20;
        c.gridx = 2;
        c.gridy = 1;
        radioButton.addActionListener(listener);
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName(nameAddMfaRadioMobileButton);
        radioButton.setText(labelMobile);
        radioButton.setVisible(false);
        phoneTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName(nameAddMfaRadioAltMobileButton);
        radioButton.setText(labelAltMobile);
        radioButton.setVisible(false);
        phoneTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 1;
        c.gridy = 2;
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName(nameAddMfaRadioOfficeButton);
        radioButton.setText(labelOffice);
        radioButton.setVisible(false);
        phoneTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 2;
        pane.add(radioButton, c);

        label = new JLabel(labelUsageExample);
        label.setName(nameAddMfaUsageExample);
        label.setVisible(false);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(label, c);

        TextField input = new TextField();
        input.setName(nameAddMfaInputText);
        c.weightx = 0.0;
        c.gridwidth = 4;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 4;
        pane.add(input, c);

        button = new JButton("Add Method");
        button.setName("addMethodButton");
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 1;
        c.gridy = 5;
        button.addActionListener(e -> addMethodButton_Click(mfaWindow));
        pane.add(button, c);

        //mfaWindow.pack();
        mfaWindow.setVisible(true);

        return mfaWindow;

    }

    public static void addMethodButton_Click(JFrame frame) {
        //get the components from the frame
        Component[] comps = frame.getContentPane().getComponents();
        String inputText = "";
        MethodType methodType = null;
        PhoneType phoneType = null;
        EmailAuthenticationMethod emailMethod = new EmailAuthenticationMethod();
        EmailAuthenticationMethod emailResult = null;
        PhoneAuthenticationMethod phoneMethod = new PhoneAuthenticationMethod();
        PhoneAuthenticationMethod phoneResult = null;

        String messageSuccessEmail = "Email method added successfully: ";
        

        // read the input and gather the selections made by the user
        for (Component comp : comps) {
            if (comp instanceof TextField textField) {
                if (textField.getName().equals(nameAddMfaInputText)) {
                    inputText = textField.getText();
                }
            } else if (comp instanceof JRadioButtonMenuItem radioButton) {
                //first check if the radio button is selected
                if (radioButton.isSelected()) {
                    if (radioButton.getName().equals(nameAddMfaRadioMobileButton)) {
                        phoneType = PhoneType.MOBILE;
                    } else if (radioButton.getName().equals(nameAddMfaRadioAltMobileButton)) {
                        phoneType = PhoneType.ALTMOBILE;
                    } else if (radioButton.getName().equals(nameAddMfaRadioOfficeButton)) {
                        phoneType = PhoneType.OFFICE;
                    } else if (radioButton.getName().equals(nameAddMfaRadioEmailButton)) {
                        methodType = MethodType.EMAIL;
                    } else if (radioButton.getName().equals(nameAddMfaRadioMobileButton)) {
                        methodType = MethodType.PHONE;
                    }
                }
            }
        }

        //create the method based on the input

        if (null == inputText || inputText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter valid contact information.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (methodType == MethodType.EMAIL && null != inputText) {
            //the user selected Email as the method type
            //get input from the user
            emailMethod.setEmailAddress(inputText);

            //send the request to add the email method
            try {
                emailResult = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                    .emailMethods().post(emailMethod);
            } catch (ODataError ex) {
                JOptionPane.showMessageDialog(frame, "Error adding email method: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            // if successful, show a message
            if (null != emailResult) {
                App.outputArea.append(messageSuccessEmail + emailResult.getEmailAddress() + "\n");
            }
            //close the frame
            frame.dispose();
        //user selected Phone but did not select a phone type
        } else if (methodType == MethodType.PHONE && null == phoneType) {
            JOptionPane.showMessageDialog(frame, "Please select a phone type.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (methodType == MethodType.PHONE && null != phoneType && !inputText.isEmpty()) {
           
            phoneMethod.setPhoneNumber(inputText);
            if (phoneType == PhoneType.MOBILE) {
                phoneMethod.setPhoneType(AuthenticationPhoneType.Mobile);
            } else if (phoneType == PhoneType.ALTMOBILE) {
                phoneMethod.setPhoneType(AuthenticationPhoneType.AlternateMobile);
            } else if (phoneType == PhoneType.OFFICE) {
                phoneMethod.setPhoneType(AuthenticationPhoneType.Office);
            }

            try {
                phoneResult = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                    .phoneMethods().post(phoneMethod);
                JOptionPane.showMessageDialog(frame, "Phone method added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ODataError ex) {
                JOptionPane.showMessageDialog(frame, "Error adding phone method: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a valid method type and enter\n." +
                "a valide method contact.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        //close the window
        if( null != phoneResult || null != emailResult)
            frame.dispose();
    }

    public static Boolean isDefaultMethod (AuthenticationMethod method) {
        Boolean isDefault = false;
        //check if the method is the default method
        String defaultMethod = getDefaultMethod();
        
        if (method instanceof MicrosoftAuthenticatorAuthenticationMethod && defaultMethod.equals(defaultPush))
            isDefault = true;
        else if (method instanceof SoftwareOathAuthenticationMethod && defaultMethod.equals(defaultOath))
            isDefault = true;
        else if (method instanceof HardwareOathAuthenticationMethod && defaultMethod.equals(defaultOath))
            isDefault = true;
        else if (method instanceof PhoneAuthenticationMethod) {
            PhoneAuthenticationMethod phoneMethod = (PhoneAuthenticationMethod) method;
            if (phoneMethod.getPhoneType() == AuthenticationPhoneType.Mobile && 
                (defaultMethod.equals(defaultSms) || defaultMethod.equals(defaultVoiceMobile)))
                isDefault = true;
            else if (phoneMethod.getPhoneType() == AuthenticationPhoneType.AlternateMobile 
                && defaultMethod.equals(defaultVoiceAltMobile))
                isDefault = true;
            else if (phoneMethod.getPhoneType() == AuthenticationPhoneType.Office 
                && defaultMethod.equals(defaultVoiceOffice))
                isDefault = true;
        }
        
        return isDefault;
    }

    public static void fillQrCodeWindow (JFrame qrCodeWindow) {
        // split out the authentication methods in qrCodeMethod to make them easier to access

        QrCode stdCode = null;
        QrCode tmpCode = null;
        QrPin qrPin = null;

        if (null != App.qrCodeMethod) {
            stdCode = App.qrCodeMethod.getStandardQRCode();
            tmpCode = App.qrCodeMethod.getTemporaryQRCode();
            qrPin = App.qrCodeMethod.getPin();
        }

        //initialize a new panes        
        Container paneStdCode = new Container();
        Container paneTmpCode = new Container();
        Container panePin = new Container();
        
        //configure the panes
        paneStdCode.setLayout(new GridBagLayout());
        paneStdCode.setBackground(qrCodeWindow.getBackground());
        paneStdCode.setName(nameStdCodePane);
        paneTmpCode.setLayout(new GridBagLayout());
        paneTmpCode.setBackground(qrCodeWindow.getBackground());
        paneTmpCode.setName(nameTmpCodePane);
        panePin.setLayout(new GridBagLayout());
        panePin.setBackground(qrCodeWindow.getBackground());
        panePin.setName(namePinPane);
        

        //create variables for other needed components
        JTabbedPane tabbedPane = new JTabbedPane();
        JLabel label;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.CENTER;
        Integer pinLength = App.qrPolicy.getPinLength();

        /////////////////////////
        //  STANDARD QR CODE PANE
        /////////////////////////
        
        //if a standard qrcode already exists, display the details
        if(null != stdCode) {
            Boolean isStandard = true;
            drawDetailsCodePane(paneStdCode, isStandard, stdCode);
        }
        // a standard code doesn't exist, show creation optoins
        else {
            drawCreateCodePane(paneStdCode);
        }

        /////////////////////////
        //  TEMPORARY QR CODE PANE
        /////////////////////////
        
        // there is no standard method (yet) so we can't do anything with the temporary pane
        if(null == stdCode) {
            label = new JLabel(messageTmpCodeNoStd);
            c.insets = insetZero;
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 1;
            paneTmpCode.add(label, c);
        }
        // there is a standard method but not a temporary one, show create options
        else if ( null != stdCode && null == tmpCode) {
            drawCreateCodePane(paneTmpCode);

        // there is a standard method and a temporary one, show details
        } else if ( null != stdCode && null != tmpCode ) {
            Boolean isStandard = false;
            drawDetailsCodePane(paneTmpCode, isStandard, tmpCode);
        }

        /////////////////////////
        //  PIN PANE
        /////////////////////////
        
        // if there is no PIN method, then there is no pin
        if (null == qrPin) {
            label = new JLabel(messagePinNoStd);
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 1;
            panePin.add(label, c);
        }
        // there is a PIN method, so we display the PIN info
        else {
            drawPinDetailsPane(panePin, qrPin, pinLength);
        }

        tabbedPane.addTab(StdCodePane, paneStdCode);
        tabbedPane.addTab(TmpCodePane, paneTmpCode);
        tabbedPane.addTab(PinCodePane, panePin);

        qrCodeWindow.add(tabbedPane);

        //qrCodeWindow.setVisible(true);
    }

    public static void resetQRCodePin(Container pane, Integer pinLength) {
        String message = "Please enter a PIN below\n" +
            "It must be " + pinLength + " digits long.\n" +
            "You may also enter \'System\' to generate one.";
        String system = "System";
        //JOptionPane.showMessageDialog(null, message);


        QrPin update = new QrPin();
        String pinInput = JOptionPane.showInputDialog(null, message);

        if(null != pinInput) {
            if(pinInput.equals(system))
                pinInput = "";
        }
        update.setCode(pinInput);
        update.setOdataType("#microsoft.graph.qrPin");

        QrPin result = null;

        try {
            result = App.graphClient.users().byUserId(App.activeUser.getId()).authentication().qrCodePinMethod().
                pin().patch(update);
        } catch (ODataError e) {
            JOptionPane.showMessageDialog(null, "Issues reseting PIN\n" + e.getMessage());
        }

        if (null != result) {
            pane.removeAll();
            drawPinDetailsPane(pane, result, pinLength);
        }


    }

    private static void drawPinDetailsPane(Container pane, QrPin qrPin, Integer pinLength) {
        JLabel label;
        JButton button;
        JTextField value;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.CENTER;

        //title row
        label = new JLabel(labelPinDisplayTitle);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(label, c);

        //ID row
        label = new JLabel(labelId);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        pane.add(label, c);

        label = new JLabel(qrPin.getId());
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        pane.add(label, c);

        //DateTime created row
        label = new JLabel(labelCreated);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(label, c);

        label = new JLabel(qrPin.getCreatedDateTime().toString());
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 2;
        pane.add(label, c);

        //DateTime updated row
        label = new JLabel(labelUpdated);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(label, c);

        label = new JLabel(qrPin.getUpdatedDateTime().toString());
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 3;
        pane.add(label, c);
        
        //ForceChange row
        label = new JLabel(labelForceChange);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 5;
        pane.add(label, c);

        label = new JLabel(qrPin.getForceChangePinNextSignIn().toString());
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 5;
        pane.add(label, c);

        //PIN view row
        label = new JLabel(labelNewPin);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 6;
        pane.add(label, c);

        if(null != qrPin.getCode())
            label = new JLabel(qrPin.getCode());
        else
            label = new JLabel("");
        label.setToolTipText(stringPinToolTip);
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 6;
        pane.add(label, c);

        //reset PIN
        button = new JButton(buttonResetPin);
        button.addActionListener(e -> resetQRCodePin(pane, pinLength));
        //c.insets = insetsButton;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 8;
        pane.add(button, c);
    }

    private static void drawCreateCodePane(Container pane) {
        
        QrPin qrPin = null;

        if (null != App.qrCodeMethod && null != App.qrCodeMethod.getPin()) {
            qrPin = App.qrCodeMethod.getPin();
        }

        Integer pinLength = App.qrPolicy.getPinLength();
        Integer defaultLifetime = App.qrPolicy.getStandardQRCodeLifetimeInDays();

        JLabel label;
        JButton button;
        JSpinner spinner;
        SpinnerModel model;
        JCheckBox checkBox;
        SpinnerDateModel spinnerModel;
        JSpinner dateSpinner;
        JSpinner.DateEditor dateEditor;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.CENTER;

        //title row
        //check the name of the pane set the label accordingly
        if (pane.getName().equals(nameStdCodePane))
            label = new JLabel(labelStdCreateTitle);
        else
            label = new JLabel(labelTmpCreateTitle);
        c.insets = insetZero;
        //c.weightx = 0.0;
        c.gridwidth = 2;
        //c.ipadx = 10;
        //c.ipady = 10;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(label, c);

        //row 2
        //  expiration row for std pand
        if (pane.getName().equals(nameStdCodePane)) {
            label = new JLabel(labelSetExp);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            pane.add(label, c);

            Date defaultExpDate = Date.from(OffsetDateTime.now().plusDays(defaultLifetime).toInstant());
            Date minExpDate = Date.from(OffsetDateTime.now().plusDays(stdMinLifeTime).toInstant());
            Date maxExpDate = Date.from(OffsetDateTime.now().plusDays(stdMaxLifeTime).toInstant());

            spinnerModel = new SpinnerDateModel(defaultExpDate, minExpDate,
                maxExpDate, java.util.Calendar.MINUTE);
            dateSpinner = new JSpinner(spinnerModel);
            dateEditor = new JSpinner.DateEditor(dateSpinner, "MM-dd-yyyy HH:mm");
            dateSpinner.setEditor(dateEditor);
            dateSpinner.setName(dateSpinnerExp);
            
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            pane.add(dateSpinner, c);
        }
        // lifetime row for Temp Codes
        else {
            label = new JLabel(labelLifeTime);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            pane.add(label, c);

            model = new SpinnerNumberModel(tmpLifeDefault,tmpLifeMin,tmpLifeMax,tmpLifeStep);
            spinner = new JSpinner(model);
            spinner.setName(spinnerTmpLife);
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            pane.add(spinner, c);
        }

        //activate later row
        checkBox = new JCheckBox(labelActivateLater);
        checkBox.setName(nameActivateLaterCheck);
        checkBox.addItemListener( e -> {
            if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                activateLater_Check(pane);
            }
        });
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(checkBox, c);

        //activate later date row
        label = new JLabel(labelActivationDate);
        label.setName(labelName);
        label.setVisible(false);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(label, c);

        Date minActDate = Date.from(OffsetDateTime.now().toInstant());
        Date defaultActDate = Date.from(OffsetDateTime.now().toInstant());
        Date maxActDate = Date.from(OffsetDateTime.now().plusDays(stdMaxLifeTime).toInstant());
        
        dateSpinner = new JSpinner(new SpinnerDateModel(defaultActDate, minActDate, maxActDate, java.util.Calendar.MINUTE));
        dateEditor = new JSpinner.DateEditor(dateSpinner, "MM-dd-yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setName(dateSpinnerAct);
        dateSpinner.setVisible(false);
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 3;
        pane.add(dateSpinner, c);

        //pin input row - only for std pane
        if(pane.getName().equals(nameStdCodePane)) {
            
            if(null != qrPin) {
                label = new JLabel(labelNoPinExplanation);
                c.gridwidth = 2;
                c.gridx = 0;
                c.gridy = 4;
                pane.add(label, c);
            }
            else {
                label = new JLabel(labelEnterPin);
                c.gridwidth = 1;
                c.gridx = 0;
                c.gridy = 4;
                pane.add(label, c);

                NumberFormat format = NumberFormat.getIntegerInstance();
                format.setGroupingUsed(false);
                format.setMinimumIntegerDigits(pinLength);
                NumberFormatter numberFormatter = new NumberFormatter(format);
                numberFormatter.setValueClass(Long.class);
                numberFormatter.setAllowsInvalid(false);

                JFormattedTextField pinInput = new JFormattedTextField(numberFormatter);
                pinInput.setToolTipText("Minimum PIN length is " + pinLength);
                pinInput.setName(namePinInput);
                pinInput.setSize(40, 10);
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridwidth = 1;
                c.gridx = 1;
                c.gridy = 4;
                pane.add(pinInput, c);
            }
        }

        //create code button

        if(pane.getName().equals(nameStdCodePane))
            button = new JButton(buttonCreateStd);
        else
            button = new JButton(buttonCreateTmp);
        button.addActionListener(e -> createCode_Click((JTabbedPane)pane.getParent(), e));
        c.fill = GridBagConstraints.CENTER;
        c.insets = insetsButton;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 5;
        pane.add(button, c);
    }
    

    private static void drawDetailsCodePane(Container pane, Boolean isStandard, QrCode code) {
        JLabel label;
        JButton button;
        GridBagConstraints c = new GridBagConstraints();

        if( null != code) {
            // title row
            if(isStandard) {
                label = new JLabel(labelStdDisplayTitle);
                c.gridwidth = 1;
                c.gridx = 1;
            } else {
                label = new JLabel(labelTmpDisplayTitle);
                c.gridwidth = 2;
                c.gridx = 0;
            }
            c.weighty = 1;
            c.gridy = 0;
            pane.add(label, c);

            if (null != code.getImage() && null != code.getImage().getBinaryValue()) {

                byte[] svgBytes = code.getImage().getBinaryValue();
                String svgXml = new String(svgBytes, java.nio.charset.StandardCharsets.UTF_8).trim();
                BufferedImage qrCodeImg = createImageFromSvgString(svgXml);

                if (null != qrCodeImg) {
                    label = new JLabel(new ImageIcon(qrCodeImg));

                    // Add right-click save functionality
                    label.setToolTipText("You can right-click to save the code.");
                    label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) showMenu(e);
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) showMenu(e);
                    }
                    private void showMenu(MouseEvent e) {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem saveItem = new JMenuItem(" Save Image...");
                        saveItem.addActionListener(ev -> {
                            ImageIcon icon = (ImageIcon) ((JLabel) e.getComponent()).getIcon();
                            if (icon != null && icon.getImage() instanceof BufferedImage) {
                                JFileChooser chooser = new JFileChooser();
                                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                    try {
                                        java.io.File file = chooser.getSelectedFile();
                                        ImageIO.write((BufferedImage) icon.getImage(), "png", file);
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(null, "Failed to save image: " + ex.getMessage());
                                    }
                                }
                            }
                        });
                        menu.add(saveItem);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    });

                }
                else
                    label = new JLabel("Unable to display QR Code Here");   
            }
            else 
                label = new JLabel();

            c.gridheight = 5;
            c.weighty = 0;
            c.gridwidth = 1;
            c.gridx = 2;
            c.gridy = 0;
            pane.add(label, c);

            //ID row
            label = new JLabel(labelId);
            c.gridheight = 1;
            c.gridwidth = 1;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            pane.add(label, c);

            label = new JLabel(code.getId().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            pane.add(label, c);

            //created row
            label = new JLabel(labelCreated);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 2;
            pane.add(label, c);

            label = new JLabel(code.getCreatedDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 2;
            pane.add(label, c);
            
            //active datetime row
            label = new JLabel(labelActive);
            c.gridheight = 1;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 4;
            pane.add(label, c);

            label = new JLabel(code.getStartDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 4;
            pane.add(label, c);

            //expires datetime row
            label = new JLabel(labelExpires);
            c.weighty = 0.0;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 5;
            pane.add(label, c);

            label = new JLabel(code.getExpireDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 5;
            pane.add(label, c);
            
            // if this is a standard method, show the change expiration button
            if (isStandard) {
                button = new JButton(buttonChangeExp);
                c.gridwidth = 1;
                c.gridx = 2;
                c.gridy = 5;
                pane.add(button, c);
            }

            label = new JLabel(labelLastUsed);
            label = new JLabel(code.getLastUsedDateTime().toLocalDateTime().toString());

            // Delete button
            if (isStandard) 
                button = new JButton(buttonDelStd);
            else
                button = new JButton(buttonDelTmp);
            //c.insets = insetsButton;
            button.addActionListener(e -> deleteCode_Click(pane, isStandard));
            c.weighty = 0.0;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 8;
            pane.add(button, c);
        }
        else
            JOptionPane.showMessageDialog(null, "Error creating pane for tab: " + pane.getName());
    }

    public static BufferedImage createImageFromSvgString(String source) {
        BufferedImage qrCodeImg = null;
        
        try {
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(new StringReader(source));
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(pngOutputStream);

            transcoder.transcode(input, output);

            byte[] pngData = pngOutputStream.toByteArray();
            qrCodeImg = ImageIO.read(new ByteArrayInputStream(pngData));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to render SVG QR code: " + ex.getMessage());
        }

        return qrCodeImg;
    }
    public static void activateLater_Check(Container pane) {
        for (Component comp : pane.getComponents()) {
            if (null == comp.getName())
                continue;
            else {//getName() resolves 
                if ( comp.getName().equals(labelName) || comp.getName().equals(dateSpinnerAct)) {
                    comp.setVisible(!comp.isVisible());
                    comp.setEnabled(true);
                }
            }
        }

        pane.repaint();
    }

    private static void createCode_Click(JTabbedPane tabbedpane, ActionEvent e) {
       
        Boolean actLater = false;
        OffsetDateTime expDate = null;
        OffsetDateTime actDate = OffsetDateTime.now();
        Integer tmpLife = tmpLifeDefault;
        String pinEntry = null;
        
        QrCode newCode = null;
        
        Container pane = ((JButton)e.getSource()).getParent();
        Window window = SwingUtilities.getWindowAncestor(pane);
        JFrame frame = null;

        if(window instanceof JFrame) {
            frame = (JFrame)window;
        }

        //JOptionPane.showMessageDialog(null,e.getSource());

        if(null != pane) {
            //update the cursor
            pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            //check if activate later is checked.
            for(Component comp : pane.getComponents()) {
                if (null != comp.getName() && comp.getName().equals(nameActivateLaterCheck)){
                    actLater = ((JCheckBox)comp).isSelected();
                    break;
                }
            }

            //get the expiration date and activation date (if necessary) components
            for(Component comp : pane.getComponents()) {
                if(comp instanceof JSpinner) {
                    if (null != comp.getName() && comp.getName().equals(dateSpinnerExp)){
                        Date temp = (Date)((JSpinner)comp).getValue();
                        expDate = OffsetDateTime.ofInstant(temp.toInstant(), java.time.ZoneId.systemDefault());
                    } else if (null != comp.getName() && comp.getName().equals(dateSpinnerAct)
                        && actLater) {
                            Date temp = (Date)((JSpinner)comp).getValue();
                            actDate = OffsetDateTime.ofInstant(temp.toInstant(), java.time.ZoneId.systemDefault());
                    } else if (null != comp.getName() && comp.getName().equals(spinnerTmpLife)) {
                        tmpLife = (Integer) ((JSpinner)comp).getValue();
                    }
                }
                else if (comp instanceof JFormattedTextField)
                    if (null != ((JFormattedTextField)comp).getValue())
                        pinEntry = ((JFormattedTextField)comp).getValue().toString();
            }

            QrCodePinAuthenticationMethod newMethod = new QrCodePinAuthenticationMethod();
            
            //if we are coming from the standard code pane
            if(pane.getName().equals(nameStdCodePane)) {
                //at most, we will have 3 things to populate:
                //  start/activation time (required)
                //  expiration time (required)
                //  pin (required on new activation)
                
                newMethod.setStandardQRCode(new QrCode());

                //expiration date is required to fill out
                newMethod.getStandardQRCode().setExpireDateTime(expDate);
                //activation date is initially set to now, but could have been updated
                newMethod.getStandardQRCode().setStartDateTime(actDate);
                
                //if pin entry is not null, we can add pin, otherwise we don't need to worry about it.
                if(null != pinEntry) {
                    newMethod.setPin(new QrPin());

                    if(pinEntry.length() >= App.qrPolicy.getPinLength()) 
                        newMethod.getPin().setCode(pinEntry);
                    else if(pinEntry.isEmpty()) {
                        Random random = new Random();
                        newMethod.getPin().setCode(Integer.toString(random.nextInt(99999998) + 1));
                    }
                    //try the request
                    if(null != newMethod.getPin().getCode()) {
                    try {
                        App.qrCodeMethod = graphCalls.createQrCodeMethod(newMethod);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    } catch (InterruptedException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }

                    if (null != frame) {
                        //frame.removeAll();
                        tabbedpane.removeAll();
                        frame.remove(tabbedpane);
                        fillQrCodeWindow(frame);
                        frame.repaint();
                        frame.revalidate();
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Critical Error");
                    }
                }
                else {
                    String message = "PIN is required to be legnth of " + App.qrPolicy.getPinLength();
                    JOptionPane.showMessageDialog(null, message);
                }
                }
                //PIN is null so it wasn't created, we are just updating the standard code
                else {
                    try {
                        newCode = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                            .qrCodePinMethod().standardQRCode().patch(newMethod.getStandardQRCode());
                    } catch (ODataError ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }

                    pane.removeAll();
                    drawDetailsCodePane(pane, true, newCode);
                    pane.repaint();
                    pane.revalidate();
                }
            }
            //we are coming from the temporary code pane
            else if(pane.getName().equals(nameTmpCodePane)) {
                newMethod.setTemporaryQRCode(new QrCode());
                //we have two things to set
                //  expiration time is activation time + tmplifetime
                //  activation time
                newMethod.getTemporaryQRCode().setExpireDateTime(actDate.plusHours(tmpLife));
                newMethod.getTemporaryQRCode().setStartDateTime(actDate);

                //try the request
                try {
                    newCode = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                        .qrCodePinMethod().temporaryQRCode().patch(newMethod.getTemporaryQRCode());
                } catch (ODataError ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }

                pane.removeAll();
                drawDetailsCodePane(pane, false, newCode);
                pane.repaint();
                pane.revalidate();
            }
            // we are only ever going to pass either tmpCodePane or stdCodePane, this shouldn't be reachable.
            else
                JOptionPane.showMessageDialog(null, "Error updating window.");   
        }

        //all done, fix the cursor
        pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private static void deleteCode_Click(Container pane, Boolean isStandard) {
        //change the cursor while we wait
        pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //delete the method();
        deleteQrCode(isStandard);

        //redraw the pane
        pane.removeAll();
        drawCreateCodePane(pane);
        pane.repaint();

        //fix the cursor
        pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private static void deleteQrCode(Boolean isStandard) {
        //standard code
        if(isStandard) {
            try {
                //delte the method
                App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                    .qrCodePinMethod().standardQRCode().delete();
                //update the method
                App.qrCodeMethod = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                    .qrCodePinMethod().get();
            }
            catch (ODataError ex) {
                JOptionPane.showMessageDialog(null, "Error deleting method.");
            }
        } //temporary code
        else {
            try {
                //delte the method
                App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                    .qrCodePinMethod().temporaryQRCode().delete();
                //update the method
                App.qrCodeMethod = App.graphClient.users().byUserId(App.activeUser.getId()).authentication()
                    .qrCodePinMethod().get();
            }
            catch (ODataError ex) {
                JOptionPane.showMessageDialog(null, "Error deleting method.");
            }
        }
    }
}

