package entra;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
//import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.microsoft.graph.beta.models.*;
//import com.microsoft.graph.beta.models.ExternalAuthenticationType;
//import com.microsoft.graph.beta.models.odataerrors.*;
import com.microsoft.graph.beta.models.odataerrors.ODataError;

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

    //set strings
        static String messageStdCodeExists = "You have an existing Standard QR Code method";
        static String messageStdCodeNew = "You need to create a new Standard QR Code method";
        static String messageTmpCodeNoStd = "You must configure a standard code before you can configure a temporary one.";
        static String messagePinNoStd = "You must configure a standard code in order to view/reset PIN information.";
        static String windowTitle = "QR Code Authentication";
        static String labelLifeTime = "LifeTime : ";
        static String labelLastUsed = "Last Used DateTime : ";
        static String labelCreated = "Created DateTime : ";
        static String labelActive = "Active DateTime : ";
        static String labelExpires = "Expiration DateTime : ";
        static String labelUpdated = "Updated DateTime : ";
        static String labelId = "Id : ";
        static String labelStdDisplayTitle = "Standard QR Code Details";
        static String labelTmpDisplayTitle = "Temporary QR Code Details";
        static String labelPinDisplayTitle = "PIN Information";
        static String labelStdCreateTitle = "Standard QR Code Creation Options";
        static String labelTmpCreateTitle = "Temporary QR Code Creation Options";
        static String labelActivationDate = "Set Activation Date : "; 
        static String labelSetExp = "Set Expiration Date : ";
        static String labelActivateLater = "Activate Later";
        static String labelName = "activateLaterLabel";
        static String labelEnterPin = "Enter PIN";
        static String spinnerName = "dateSpinner";
        static String dateSpinnerExp = "dateSpinnerExp";

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
                                message.append(" **Default Method**\n");
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
                                message.append(" **Default Method**\n");
                            else if (defaultVoiceMobile == defaultMethod && phoneTypeMobile == phoneMethod.getPhoneType()) 
                                message.append(" **Default Method**\n");
                            else if (defaultVoiceAltMobile == defaultMethod && phoneTypeAltMobile == phoneMethod.getPhoneType()) 
                                message.append(" **Default Method**\n");  
                            else if (defaultVoiceOffice == defaultMethod && phoneTypeOffice == phoneMethod.getPhoneType()) 
                                message.append(" **Default Method**\n"); 
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
                            message.append("\nQR Code PIN Method: \n");
                            
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

        var MFAList = getUserMfaMethods();

        for (AuthenticationMethod method : MFAList) {
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
                        JOptionPane.showMessageDialog(null, "Unable to delete passwords.", errorTitle, 0);
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
        checkBox.setToolTipText("If this box is disabled, one-time-use is required by your settings.");
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
        }

        //close the window
        frame.dispose();

    }

    public static void createAddMethodWindow() {
        //initialize a new JFrame and the pane
        JFrame mfaWindow = new JFrame();
        Container pane = mfaWindow.getContentPane();
        
        //set strings
        String message01 = "Please select the type of authentication method to add.";
        String message02 = "Please enter the number as +1 1234567890";
        String title = "Add Authentication Method";

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
                if (source.getName().equals("radioMobileButton")) {
                    phoneTypeGroup.clearSelection();
                    source.setSelected(true);

                    // Show the phone type options
                    Component[] components = pane.getComponents();
                    for (Component comp : components) {
                        if (null == comp.getName()) {
                            continue; // Skip components without a name
                        } else if (comp.getName().equals("radioMobilePhoneButton")) {
                            comp.setVisible(true);
                        } else if (comp.getName().equals("radioAltMobilePhoneButton")) {
                            comp.setVisible(true);
                        } else if (comp.getName().equals("radioOfficeButton")) {
                            comp.setVisible(true);
                        } else if (comp.getName().equals("message02")) {
                            comp.setVisible(true);
                        }
                    }   
                } else if (source.getName().equals("radioEmailButton")) {
                    phoneTypeGroup.clearSelection();
                    source.setSelected(true);

                    Component[] components = pane.getComponents();
                    for (Component comp : components) {
                        if (null == comp.getName()) {
                            continue; // Skip components without a name
                        } else if (comp.getName().equals("radioMobilePhoneButton")) {
                            comp.setVisible(false);
                        } else if (comp.getName().equals("radioAltMobilePhoneButton")) {
                            comp.setVisible(false);
                        } else if (comp.getName().equals("radioOfficeButton")) {
                            comp.setVisible(false);
                        } else if (comp.getName().equals("message02")) {
                            comp.setVisible(false);
                        }
                    } 
                }
            }
        };
        
        label = new JLabel(message01);
        c.weightx = 0.0;
        c.gridwidth = 3;
        c.ipadx = 10;
        c.ipady = 10;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(label, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName("radioMobileButton");
        radioButton.setText("Phone");
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
        radioButton.setName("radioEmailButton");
        radioButton.setText("Email");
        methodTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 20;
        c.gridx = 2;
        c.gridy = 1;
        radioButton.addActionListener(listener);
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName("radioMobilePhoneButton");
        radioButton.setText("Mobile");
        radioButton.setVisible(false);
        phoneTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName("radioAltMobilePhoneButton");
        radioButton.setText("Alternate Mobile");
        radioButton.setVisible(false);
        phoneTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 1;
        c.gridy = 2;
        pane.add(radioButton, c);

        radioButton = new JRadioButtonMenuItem();
        radioButton.setName("radioOfficeButton");
        radioButton.setText("Office");
        radioButton.setVisible(false);
        phoneTypeGroup.add(radioButton);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 2;
        pane.add(radioButton, c);

        label = new JLabel(message02);
        label.setName("message02");
        label.setVisible(false);
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(label, c);

        TextField input = new TextField();
        input.setName("inputText");
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

    }

    public static void addMethodButton_Click(JFrame frame) {
        //get the components from the frame
        Component[] comps = frame.getContentPane().getComponents();
        String inputText = "";
        String methodType = "";
        String phoneType = "";
        EmailAuthenticationMethod emailMethod = new EmailAuthenticationMethod();
        EmailAuthenticationMethod emailResult = null;
        PhoneAuthenticationMethod phoneMethod = new PhoneAuthenticationMethod();
        PhoneAuthenticationMethod phoneResult = null;
        

        // read the input and gather the selections made by the user
        for (Component comp : comps) {
            if (comp instanceof TextField textField) {
                if (textField.getName().equals("inputText")) {
                    inputText = textField.getText();
                }
            } else if (comp instanceof JRadioButtonMenuItem radioButton) {
                //first check if the radio button is selected
                if (radioButton.isSelected()) {
                    if (radioButton.getName().equals("radioMobilePhoneButton")) {
                        phoneType = "Mobile";
                    } else if (radioButton.getName().equals("radioAltMobilePhoneButton")) {
                        phoneType = "Alternate Mobile";
                    } else if (radioButton.getName().equals("radioOfficeButton")) {
                        phoneType = "Office";
                    } else if (radioButton.getName().equals("radioEmailButton")) {
                        methodType = "Email";
                    } else if (radioButton.getName().equals("radioMobileButton")) {
                        methodType = "Phone";
                    }
                }
            }
        }

        //create the method based on the input

        if (null == inputText || inputText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter valid contact information.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (methodType.equals("Email") && null != inputText) {
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
            if (null != emailMethod) {
                App.outputArea.append("Email method added successfully: " + emailMethod.getEmailAddress() + "\n");
            }
            //close the frame
            frame.dispose();
        //user selected Phone but did not select a phone type
        } else if (methodType.equals("Phone") && phoneType.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select a phone type.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (methodType.equals("Phone") && !phoneType.isEmpty() && !inputText.isEmpty()) {
           
            phoneMethod.setPhoneNumber(inputText);
            if (phoneType.equals("Mobile")) {
                phoneMethod.setPhoneType(AuthenticationPhoneType.Mobile);
            } else if (phoneType.equals("Alternate Mobile")) {
                phoneMethod.setPhoneType(AuthenticationPhoneType.AlternateMobile);
            } else if (phoneType.equals("Office")) {
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
        
        if (method instanceof MicrosoftAuthenticatorAuthenticationMethod && defaultMethod.equals("push"))
            isDefault = true;
        else if (method instanceof SoftwareOathAuthenticationMethod && defaultMethod.equals("oath"))
            isDefault = true;
        else if (method instanceof HardwareOathAuthenticationMethod && defaultMethod.equals("oath"))
            isDefault = true;
        else if (method instanceof PhoneAuthenticationMethod) {
            PhoneAuthenticationMethod phoneMethod = (PhoneAuthenticationMethod) method;
            if (phoneMethod.getPhoneType() == AuthenticationPhoneType.Mobile && 
                (defaultMethod.equals("sms") || defaultMethod.equals("voiceMobile")))
                isDefault = true;
            else if (phoneMethod.getPhoneType() == AuthenticationPhoneType.AlternateMobile 
                && defaultMethod.equals("voiceAlternateMobile"))
                isDefault = true;
            else if (phoneMethod.getPhoneType() == AuthenticationPhoneType.Office 
                && defaultMethod.equals("voiceOffice"))
                isDefault = true;
        }
        
        return isDefault;
    }

    public static void createQrCodeWindow (QrCodePinAuthenticationMethodConfiguration qrPolicy, 
        QrCodePinAuthenticationMethod qrCodeMethod) {
        // split out the authentication methods in qrCodeMethod to make them easier to access

        QrCode stdCode = null;
        QrCode tmpCode = null;
        QrPin qrPin = null;

        if (null != qrCodeMethod) {
            stdCode = qrCodeMethod.getStandardQRCode();
            tmpCode = qrCodeMethod.getTemporaryQRCode();
            qrPin = qrCodeMethod.getPin();
        }
        Integer pinLength = qrPolicy.getPinLength();
        Integer defaultLifetime = qrPolicy.getStandardQRCodeLifetimeInDays();
        Integer stdMinLifeTime = 1;
        Integer stdMaxLifeTime = 395;
        Integer tmpLifeMin = 1;
        Integer tmpLifeMax = 12;
        Integer tmpLifeDefault = 3;
        Integer tmpLifeStep = 1;

        //initialize a new JFrame and the pane
        JFrame qrCodeWindow = new JFrame();
        Container paneStdCode = new Container();
        Container paneTmpCode = new Container();
        Container panePin = new Container();
        Insets insetsButton = new Insets(0,50,0,50);
        Insets insetZero = new Insets(0,0,0,0);

        

        //Configure the new window
        qrCodeWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        qrCodeWindow.setSize(600,250);
        qrCodeWindow.setLayout(new GridLayout(1,1));
        qrCodeWindow.setTitle(windowTitle);
        paneStdCode.setLayout(new GridBagLayout());
        paneStdCode.setBackground(qrCodeWindow.getBackground());
        paneTmpCode.setLayout(new GridBagLayout());
        paneTmpCode.setBackground(qrCodeWindow.getBackground());
        panePin.setLayout(new GridBagLayout());
        panePin.setBackground(qrCodeWindow.getBackground());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        //create variables for the needed components
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JLabel label;
        JButton button;
        JSpinner spinner;
        SpinnerModel model;
        JCheckBox checkBox;

        /////////////////////////
        //  STANDARD QR CODE PANE
        /////////////////////////
        
        //if a standard qrcode already exists, display the details
        if(null != stdCode) {
            // title row
            label = new JLabel(labelStdDisplayTitle);
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 0;
            paneStdCode.add(label, c);

            //ID row
            label = new JLabel(labelId);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            paneStdCode.add(label, c);

            label = new JLabel(stdCode.getId().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            paneStdCode.add(label, c);

            //created row
            label = new JLabel(labelCreated);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 2;
            paneStdCode.add(label, c);

            label = new JLabel(stdCode.getCreatedDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 2;
            paneStdCode.add(label, c);

            BufferedImage qrCode = null;

            if (null != stdCode.getImage() && stdCode.getImage().getBinaryValue() != null) {
                try {
                    qrCode = ImageIO.read(new java.io.ByteArrayInputStream(stdCode.getImage().getBinaryValue()));
                } catch (Exception e) {
                    qrCode = null;
                }

                label = new JLabel(new ImageIcon(qrCode));
                c.gridheight = 3;
                c.gridwidth = 1;
                c.gridx = 2;
                c.gridy = 3;
                paneStdCode.add(label, c);
            }

            //active datetime row
            label = new JLabel(labelActive);
            c.gridheight = 1;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 4;
            paneStdCode.add(label, c);

            label = new JLabel(stdCode.getStartDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 4;
            paneStdCode.add(label, c);

            //expires datetime row
            label = new JLabel(labelExpires);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 5;
            paneStdCode.add(label, c);

            label = new JLabel(stdCode.getExpireDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 5;
            paneStdCode.add(label, c);
            
            button = new JButton("Change Expiration");
            c.gridwidth = 1;
            c.gridx = 2;
            c.gridy = 5;
            paneStdCode.add(button, c);

            label = new JLabel(labelLastUsed);
            label = new JLabel(stdCode.getLastUsedDateTime().toString());

            // Delete button
            button = new JButton("Delete Standard QR Code");
            c.insets = insetsButton;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 8;
            paneStdCode.add(button, c);
        }
        // a standard code doesn't exist, show creation optoins
        else {
            //title row
            label = new JLabel(labelStdCreateTitle);
            c.insets = insetZero;
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 0;
            paneStdCode.add(label, c);

            // Expiration row
            label = new JLabel(labelSetExp);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            paneStdCode.add(label, c);

            Date defaultExpDate = Date.from(OffsetDateTime.now().plusDays(defaultLifetime).toInstant());

            SpinnerDateModel spinnerModel = new SpinnerDateModel(defaultExpDate, // initial value
                Date.from(OffsetDateTime.now().plusDays(stdMinLifeTime).toInstant()), // start time
                Date.from(OffsetDateTime.now().plusDays(stdMaxLifeTime).toInstant()), //end time
                java.util.Calendar.MINUTE);
            JSpinner dateSpinner = new JSpinner(spinnerModel);
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MM-dd-yyyy HH:mm");
            dateSpinner.setEditor(dateEditor);
            dateSpinner.setName(dateSpinnerExp);
            
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            paneStdCode.add(dateSpinner, c);

            //activate later row
            checkBox = new JCheckBox(labelActivateLater);
            checkBox.addItemListener( e -> {
                if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    activateLater_Check(paneStdCode, c);
                }
            });
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 2;
            paneStdCode.add(checkBox, c);

            //activate later row
            label = new JLabel(labelActivationDate);
            label.setName(labelName);
            label.setVisible(false);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 3;
            paneStdCode.add(label, c);

            Date defaultActDate = Date.from(OffsetDateTime.now().toInstant());
            Date minActDate = Date.from(OffsetDateTime.now().toInstant());
            Date maxActDate = Date.from(OffsetDateTime.now().plusDays(30).toInstant());
            
            dateSpinner = new JSpinner(new SpinnerDateModel());
            dateEditor = new JSpinner.DateEditor(dateSpinner, "MM-dd-yyyy HH:mm");
            dateSpinner.setEditor(dateEditor);
            dateSpinner.setName(spinnerName);
            dateSpinner.setVisible(false);
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 3;
            paneStdCode.add(dateSpinner, c);

            //pin input row
            label = new JLabel("Enter PIN");
            c.weightx = 0.0;
            c.gridwidth = 1;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 4;
            paneStdCode.add(label, c);

            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            NumberFormatter numberFormatter = new NumberFormatter(format);
            numberFormatter.setValueClass(Long.class);
            numberFormatter.setAllowsInvalid(false);

            JFormattedTextField pinInput = new JFormattedTextField(numberFormatter);
            pinInput.setToolTipText("Minimum PIN length is " + pinLength);
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 4;
            paneStdCode.add(pinInput, c);

            //create code button
            button = new JButton("Create QR Code");
            c.insets = insetsButton;
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 5;
            paneStdCode.add(button, c);
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
            c.insets = new Insets(0,0,0,0);
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 1;
            paneTmpCode.add(label, c);
        }
        // there is a standard method but not a temporary one, show create options
        else if ( null != stdCode && null == tmpCode) {
            //title
            label = new JLabel(labelTmpCreateTitle);
            c.insets = new Insets(0,0,0,0);
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 0;
            paneTmpCode.add(label, c);

            // lifetime row
            label = new JLabel(labelLifeTime);
            c.weightx = 0.0;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            paneTmpCode.add(label, c);

            model = new SpinnerNumberModel(tmpLifeDefault,tmpLifeMin,tmpLifeMax,tmpLifeStep);
            spinner = new JSpinner(model);
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            paneTmpCode.add(spinner, c);

            //activate later row
            checkBox = new JCheckBox(labelActivateLater);
            checkBox.addItemListener( e -> {
                if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    activateLater_Check(paneTmpCode, c);
                }
            });
            c.gridwidth = 2;
            c.gridx = 1;
            c.gridy = 2;
            paneTmpCode.add(checkBox, c);

            //leave row 3 empty

            //pin input row
            label = new JLabel(labelEnterPin);
            c.weightx = 0.0;
            c.gridwidth = 1;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 4;
            paneTmpCode.add(label, c);

            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            NumberFormatter numberFormatter = new NumberFormatter(format);
            numberFormatter.setValueClass(Long.class);
            numberFormatter.setAllowsInvalid(false);

            JFormattedTextField pinInput = new JFormattedTextField(numberFormatter);
            pinInput.setToolTipText("Minimum PIN length is " + pinLength);
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 4;
            paneTmpCode.add(spinner, c);

            button = new JButton("Create QR Code");
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 5;
            paneTmpCode.add(button, c);

        // there is a standard method and a temporary one, show details
        } else if ( null != stdCode && null != tmpCode ) {
            // title row
            label = new JLabel(labelTmpDisplayTitle);
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 0;
            paneTmpCode.add(label, c);

            //ID row
            label = new JLabel(labelId);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            paneTmpCode.add(label, c);

            label = new JLabel(tmpCode.getId());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            paneTmpCode.add(label, c);

            //created row
            label = new JLabel(labelCreated);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 2;
            paneTmpCode.add(label, c);

            label = new JLabel(tmpCode.getCreatedDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 2;
            paneTmpCode.add(label, c);

            //active datetime row
            label = new JLabel(labelActive);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 3;
            paneTmpCode.add(label, c);

            label = new JLabel(tmpCode.getStartDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 3;
            paneTmpCode.add(label, c);

            //expires datetime row
            label = new JLabel(labelExpires);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 4;
            paneTmpCode.add(label, c);

            label = new JLabel(tmpCode.getExpireDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 4;
            paneTmpCode.add(label, c);

            //last used row
            label = new JLabel(labelLastUsed);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 5;
            paneTmpCode.add(label, c);

            label = new JLabel(tmpCode.getLastUsedDateTime().toLocalDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 5;
            paneTmpCode.add(label, c);

            // Delete button
            button = new JButton("Delete Temporary QR Code");
            c.insets = insetsButton;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 8;
            paneTmpCode.add(button, c);

            
        }

        /////////////////////////
        //  PIN PANE
        /////////////////////////
        
        // if there is no standard method, then there is no pin
        if (null == stdCode || null == qrPin) {
            label = new JLabel(messagePinNoStd);
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 1;
            panePin.add(label, c);
        }
        // there is a standard method, so we display the PIN info
        else {
            label = new JLabel(labelPinDisplayTitle);
            c.weightx = 0.0;
            c.gridwidth = 2;
            c.ipadx = 10;
            c.ipady = 10;
            c.gridx = 0;
            c.gridy = 0;
            panePin.add(label, c);

            label = new JLabel(labelId);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 1;
            panePin.add(label, c);

            label = new JLabel(qrPin.getId());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 1;
            panePin.add(label, c);

            label = new JLabel(labelCreated);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 2;
            panePin.add(label, c);

            label = new JLabel(qrPin.getCreatedDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 2;
            panePin.add(label, c);

            label = new JLabel(labelUpdated);
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 3;
            panePin.add(label, c);

            label = new JLabel(qrPin.getUpdatedDateTime().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 3;
            panePin.add(label, c);

            label = new JLabel("Force Change on next signin:");
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 4;
            panePin.add(label, c);

            label = new JLabel(qrPin.getForceChangePinNextSignIn().toString());
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 4;
            panePin.add(label, c);

            button = new JButton("Reset PIN");
            button.addActionListener(e -> resetQRCodePin());
            c.insets = insetsButton;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 5;
            panePin.add(button, c);
        }
        tabbedPane.addTab("Standard QR Code", paneStdCode);
        tabbedPane.addTab("Temporary QR Code", paneTmpCode);
        tabbedPane.addTab("Pin", panePin);

        qrCodeWindow.add(tabbedPane);

        qrCodeWindow.setVisible(true);
    }

    public static void resetQRCodePin() {
        JOptionPane.showMessageDialog(null, "Create Code button was clicked");
    }

    private static void drawDetailsPane(Container pane, Boolean isStandard) {

    }
    public static void activateLater_Check(Container pane, GridBagConstraints c) {
        for (Component comp : pane.getComponents()) {
            if (null == comp.getName())
                continue;
            else {//getName() resolves 
                if ( comp.getName().equals(labelName) || comp.getName().equals(spinnerName)) {
                    comp.setVisible(!comp.isVisible());
                }
            }
        }

        pane.repaint();
    }
    

    public static void deactivateLater_Check(Container pane, GridBagConstraints c) {
        for (Component comp : pane.getComponents()) {
            if (null == comp.getName())
                continue;
            else {//getName() resolse 
                if ( comp.getName().equals(labelName) || comp.getName().equals(spinnerName)) {
                    comp.setVisible(!comp.isVisible());
                }
            }
        }

        pane.repaint();
    }
}

