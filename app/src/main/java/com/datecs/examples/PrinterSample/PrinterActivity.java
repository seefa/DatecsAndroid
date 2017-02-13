package com.datecs.examples.PrinterSample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adpa.printer.wrapper.DatecsStyle;
import com.adpa.printer.wrapper.DatecsWrapper;
import com.datecs.api.BuildInfo;
import com.datecs.api.biometric.AnsiIso;
import com.datecs.api.biometric.TouchChip;
import com.datecs.api.biometric.TouchChip.Identity;
import com.datecs.api.biometric.TouchChip.ImageReceiver;
import com.datecs.api.biometric.TouchChipException;
import com.datecs.api.card.FinancialCard;
import com.datecs.api.emsr.EMSR;
import com.datecs.api.emsr.EMSR.EMSRInformation;
import com.datecs.api.emsr.EMSR.EMSRKeyInformation;
import com.datecs.api.printer.Printer;
import com.datecs.api.printer.Printer.ConnectionListener;
import com.datecs.api.printer.PrinterInformation;
import com.datecs.api.printer.ProtocolAdapter;
import com.datecs.api.rfid.ContactlessCard;
import com.datecs.api.rfid.FeliCaCard;
import com.datecs.api.rfid.ISO14443Card;
import com.datecs.api.rfid.ISO15693Card;
import com.datecs.api.rfid.RC663;
import com.datecs.api.rfid.RC663.CardListener;
import com.datecs.api.rfid.STSRICard;
import com.datecs.api.universalreader.UniversalReader;
import com.datecs.examples.PrinterSample.network.PrinterServer;
import com.datecs.examples.PrinterSample.network.PrinterServerListener;
import com.datecs.examples.PrinterSample.util.HexUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrinterActivity extends Activity {

    private static final String LOG_TAG = "PrinterSample";

    // Request to get the bluetooth device
    private static final int REQUEST_GET_DEVICE = 0;

    // Request to get the bluetooth device
    private static final int DEFAULT_NETWORK_PORT = 9100;

    // Interface, used to invoke asynchronous printer operation.
    private interface PrinterRunnable {
        void run(ProgressDialog dialog, Printer printer) throws IOException;
    }

    // Member variables
    private ProtocolAdapter mProtocolAdapter;
    private ProtocolAdapter.Channel mPrinterChannel;
    private ProtocolAdapter.Channel mUniversalChannel;
    private Printer mPrinter;
    private EMSR mEMSR;
    private PrinterServer mPrinterServer;
    private BluetoothSocket mBtSocket;
    private Socket mNetSocket;
    private RC663 mRC663;
    private String m_Text = "";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        // Show Android device information and API version.
        final TextView txtVersion = (TextView) findViewById(R.id.txt_version);
        txtVersion.setText(Build.MANUFACTURER + " " + Build.MODEL + ", Datecs API "
                + BuildInfo.VERSION);

        findViewById(R.id.btn_read_information).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readInformation();
            }
        });

        findViewById(R.id.btn_print_self_test).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printSelfTest();
            }
        });

        findViewById(R.id.btn_print_text).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printText2();
//                List<String> list= PersianFormatUtilOrginal.printWindows1256Characters();
//                    printCustomText1(list);

            }
        });

        findViewById(R.id.btn_print_custom).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PrinterActivity.this);
                builder.setTitle("CodeTable 24");

// Set up the input
                final EditText input = new EditText(PrinterActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("تاييد", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        printCustomText(m_Text);
                    }
                });
                builder.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });

        findViewById(R.id.btn_print_image).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printImage();
            }
        });

        findViewById(R.id.btn_print_page).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printPage();
            }
        });

        findViewById(R.id.btn_print_barcode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printBarcode();
            }
        });

        findViewById(R.id.btn_read_card).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readCard();
            }
        });

        findViewById(R.id.btn_read_barcode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Read barcode waiting 10 seconds
                readBarcode(10);
            }
        });

        findViewById(R.id.btn_fingerprint).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.content_buttons).setVisibility(View.INVISIBLE);
                findViewById(R.id.content_fingerprint).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btn_fingerprint_enrol).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String identity = ((EditText) findViewById(R.id.edit_fingerprint_identity)).getText().toString();
                int identityLen = identity.length();

                if (identityLen >= 1 && identityLen <= 100) {
                    ((FingerprintView) findViewById(R.id.fingerprint)).setText(""); // Clear content
                    enrolNewIdentity(identity);
                } else {
                    toast(getString(R.string.msg_invalid_identity));
                }
            }
        });

        findViewById(R.id.btn_fingerprint_delete_all).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllIdentities();
            }
        });

        findViewById(R.id.btn_fingerprint_check).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FingerprintView) findViewById(R.id.fingerprint)).setText(""); // Clear content
                checkIdentity();
            }
        });

        findViewById(R.id.btn_fingerprint_get).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FingerprintView) findViewById(R.id.fingerprint)).setText(""); // Clear content
                getIdentity();
            }
        });

        waitForConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeActiveConnection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GET_DEVICE) {
            if (resultCode == DeviceListActivity.RESULT_OK) {
                String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // address = "192.168.11.136:9100";
                if (BluetoothAdapter.checkBluetoothAddress(address)) {
                    establishBluetoothConnection(address);
                } else {
                    establishNetworkConnection(address);
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.content_buttons).getVisibility() == View.INVISIBLE) {
            findViewById(R.id.content_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.content_fingerprint).setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void toast(final String text) {
        Log.d(LOG_TAG, text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void error(final String text) {
        Log.w(LOG_TAG, text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void dialog(final int iconResId, final String title, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(PrinterActivity.this);
                builder.setIcon(iconResId);
                builder.setTitle(title);
                builder.setMessage(msg);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dlg = builder.create();
                dlg.show();
            }
        });
    }

    private void status(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (text != null) {
                    findViewById(R.id.panel_status).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.txt_status)).setText(text);
                } else {
                    findViewById(R.id.panel_status).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void runTask(final PrinterRunnable r, final int msgResId) {
        final ProgressDialog dialog = new ProgressDialog(PrinterActivity.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(msgResId));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    r.run(dialog, mPrinter);
                } catch (IOException e) {
                    e.printStackTrace();
                    error("I/O error occurs: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    error("Critical error occurs: " + e.getMessage());
                    finish();
                } finally {
                    dialog.dismiss();
                }
            }
        });
        t.start();
    }

    protected void initPrinter(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        Log.d(LOG_TAG, "Initialize printer...");

        // Here you can enable various debug information
        //ProtocolAdapter.setDebug(true);
        Printer.setDebug(true);
        EMSR.setDebug(true);

        // Check if printer is into protocol mode. Ones the object is created it can not be released
        // without closing base streams.
        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);
        if (mProtocolAdapter.isProtocolEnabled()) {
            Log.d(LOG_TAG, "Protocol mode is enabled");

            // Into protocol mode we can callbacks to receive printer notifications
            mProtocolAdapter.setPrinterListener(new ProtocolAdapter.PrinterListener() {
                @Override
                public void onThermalHeadStateChanged(boolean overheated) {
                    if (overheated) {
                        Log.d(LOG_TAG, "Thermal head is overheated");
                        status("OVERHEATED");
                    } else {
                        status(null);
                    }
                }

                @Override
                public void onPaperStateChanged(boolean hasPaper) {
                    if (hasPaper) {
                        Log.d(LOG_TAG, "Event: Paper out");
                        status("PAPER OUT");
                    } else {
                        status(null);
                    }
                }

                @Override
                public void onBatteryStateChanged(boolean lowBattery) {
                    if (lowBattery) {
                        Log.d(LOG_TAG, "Low battery");
                        status("LOW BATTERY");
                    } else {
                        status(null);
                    }
                }
            });

            mProtocolAdapter.setBarcodeListener(new ProtocolAdapter.BarcodeListener() {
                @Override
                public void onReadBarcode() {
                    Log.d(LOG_TAG, "On read barcode");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readBarcode(0);
                        }
                    });
                }
            });

            mProtocolAdapter.setCardListener(new ProtocolAdapter.CardListener() {
                @Override
                public void onReadCard(boolean encrypted) {
                    Log.d(LOG_TAG, "On read card(entrypted=" + encrypted + ")");

                    if (encrypted) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                readCardEncrypted();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                readCard();
                            }
                        });
                    }
                }
            });

            // Get printer instance
            mPrinterChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            mPrinter = new Printer(mPrinterChannel.getInputStream(), mPrinterChannel.getOutputStream());

            // Check if printer has encrypted magnetic head
            ProtocolAdapter.Channel emsrChannel = mProtocolAdapter
                    .getChannel(ProtocolAdapter.CHANNEL_EMSR);
            try {
                // Close channel silently if it is already opened.
                try {
                    emsrChannel.close();
                } catch (IOException e) {
                }

                // Try to open EMSR channel. If method failed, then probably EMSR is not supported
                // on this device.
                emsrChannel.open();

                mEMSR = new EMSR(emsrChannel.getInputStream(), emsrChannel.getOutputStream());
                EMSRKeyInformation keyInfo = mEMSR.getKeyInformation(EMSR.KEY_AES_DATA_ENCRYPTION);
                if (!keyInfo.tampered && keyInfo.version == 0) {
                    Log.d(LOG_TAG, "Missing encryption key");
                    // If key version is zero we can load a new key in plain mode.
                    byte[] keyData = CryptographyHelper.createKeyExchangeBlock(0xFF,
                            EMSR.KEY_AES_DATA_ENCRYPTION, 1, CryptographyHelper.AES_DATA_KEY_BYTES,
                            null);
                    mEMSR.loadKey(keyData);
                }
                mEMSR.setEncryptionType(EMSR.ENCRYPTION_TYPE_AES256);
                mEMSR.enable();
                Log.d(LOG_TAG, "Encrypted magnetic stripe reader is available");
            } catch (IOException e) {
                if (mEMSR != null) {
                    mEMSR.close();
                    mEMSR = null;
                }
            }

            // Check if printer has encrypted magnetic head
            ProtocolAdapter.Channel rfidChannel = mProtocolAdapter
                    .getChannel(ProtocolAdapter.CHANNEL_RFID);

            try {
                // Close channel silently if it is already opened.
                try {
                    rfidChannel.close();
                } catch (IOException e) {
                }

                // Try to open RFID channel. If method failed, then probably RFID is not supported
                // on this device.
                rfidChannel.open();

                mRC663 = new RC663(rfidChannel.getInputStream(), rfidChannel.getOutputStream());
                mRC663.setCardListener(new CardListener() {
                    @Override
                    public void onCardDetect(ContactlessCard card) {
                        processContactlessCard(card);
                    }
                });
                mRC663.enable();
                Log.d(LOG_TAG, "RC663 reader is available");
            } catch (IOException e) {
                if (mRC663 != null) {
                    mRC663.close();
                    mRC663 = null;
                }
            }

            // Check if printer has encrypted magnetic head
            mUniversalChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_UNIVERSAL_READER);
            new UniversalReader(mUniversalChannel.getInputStream(), mUniversalChannel.getOutputStream());

        } else {
            Log.d(LOG_TAG, "Protocol mode is disabled");

            // Protocol mode it not enables, so we should use the row streams.
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(),
                    mProtocolAdapter.getRawOutputStream());
        }

        mPrinter.setConnectionListener(new ConnectionListener() {
            @Override
            public void onDisconnect() {
                toast("Printer is disconnected");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            waitForConnection();
                        }
                    }
                });
            }
        });

    }

    private synchronized void waitForConnection() {
        status(null);

        closeActiveConnection();

        // Show dialog to select a Bluetooth device.
        startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_GET_DEVICE);

        // Start server to listen for network connection.
        try {
            mPrinterServer = new PrinterServer(new PrinterServerListener() {
                @Override
                public void onConnect(Socket socket) {
                    Log.d(LOG_TAG, "Accept connection from "
                            + socket.getRemoteSocketAddress().toString());

                    // Close Bluetooth selection dialog
                    finishActivity(REQUEST_GET_DEVICE);

                    mNetSocket = socket;
                    try {
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();
                        initPrinter(in, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                        error("FAILED to initialize: " + e.getMessage());
                        waitForConnection();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void establishBluetoothConnection(final String address) {
        final ProgressDialog dialog = new ProgressDialog(PrinterActivity.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(R.string.msg_connecting));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        closePrinterServer();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Connecting to " + address + "...");

                btAdapter.cancelDiscovery();

                try {
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);

                    InputStream in = null;
                    OutputStream out = null;

                    try {
                        BluetoothSocket btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
                        btSocket.connect();

                        mBtSocket = btSocket;
                        in = mBtSocket.getInputStream();
                        out = mBtSocket.getOutputStream();
                    } catch (IOException e) {
                        error("FAILED to connect: " + e.getMessage());
                        waitForConnection();
                        return;
                    }

                    try {
                        initPrinter(in, out);
                    } catch (IOException e) {
                        error("FAILED to initiallize: " + e.getMessage());
                        return;
                    }
                } finally {
                    dialog.dismiss();
                }
            }
        });
        t.start();
    }

    private void establishNetworkConnection(final String address) {
        closePrinterServer();

        final ProgressDialog dialog = new ProgressDialog(PrinterActivity.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(R.string.msg_connecting));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        closePrinterServer();

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Connectiong to " + address + "...");
                try {
                    Socket s = null;
                    try {
                        String[] url = address.split(":");
                        int port = DEFAULT_NETWORK_PORT;

                        try {
                            if (url.length > 1) {
                                port = Integer.parseInt(url[1]);
                            }
                        } catch (NumberFormatException e) {
                        }

                        s = new Socket(url[0], port);
                        s.setKeepAlive(true);
                        s.setTcpNoDelay(true);
                    } catch (UnknownHostException e) {
                        error("FAILED to connect: " + e.getMessage());
                        waitForConnection();
                        return;
                    } catch (IOException e) {
                        error("FAILED to connect: " + e.getMessage());
                        waitForConnection();
                        return;
                    }

                    InputStream in = null;
                    OutputStream out = null;

                    try {
                        mNetSocket = s;
                        in = mNetSocket.getInputStream();
                        out = mNetSocket.getOutputStream();
                    } catch (IOException e) {
                        error("FAILED to connect: " + e.getMessage());
                        waitForConnection();
                        return;
                    }

                    try {
                        initPrinter(in, out);
                    } catch (IOException e) {
                        error("FAILED to initiallize: " + e.getMessage());
                        return;
                    }
                } finally {
                    dialog.dismiss();
                }
            }
        });
        t.start();
    }

    private synchronized void closeBluetoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBtSocket;
        mBtSocket = null;
        if (s != null) {
            Log.d(LOG_TAG, "Close Bluetooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeNetworkConnection() {
        // Close network connection
        Socket s = mNetSocket;
        mNetSocket = null;
        if (s != null) {
            Log.d(LOG_TAG, "Close Network socket");
            try {
                s.shutdownInput();
                s.shutdownOutput();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closePrinterServer() {
        closeNetworkConnection();

        // Close network server
        PrinterServer ps = mPrinterServer;
        mPrinterServer = null;
        if (ps != null) {
            Log.d(LOG_TAG, "Close Network server");
            try {
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closePrinterConnection() {
        if (mRC663 != null) {
            try {
                mRC663.disable();
            } catch (IOException e) {
            }

            mRC663.close();
        }

        if (mEMSR != null) {
            mEMSR.close();
        }

        if (mPrinter != null) {
            mPrinter.close();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.close();
        }
    }

    private synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBluetoothConnection();
        closeNetworkConnection();
        closePrinterServer();
    }

    private void readInformation() {
        Log.d(LOG_TAG, "Read information");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                StringBuffer textBuffer = new StringBuffer();
                PrinterInformation pi = printer.getInformation();

                textBuffer.append("PRINTER:");
                textBuffer.append("\n");
                textBuffer.append("Name: " + pi.getName());
                textBuffer.append("\n");
                textBuffer.append("Version: " + pi.getFirmwareVersionString());
                textBuffer.append("\n");
                textBuffer.append("\n");

                if (mEMSR != null) {
                    EMSRInformation devInfo = mEMSR.getInformation();
                    EMSRKeyInformation kekInfo = mEMSR.getKeyInformation(EMSR.KEY_AES_KEK);
                    EMSRKeyInformation aesInfo = mEMSR.getKeyInformation(EMSR.KEY_AES_DATA_ENCRYPTION);
                    EMSRKeyInformation desInfo = mEMSR.getKeyInformation(EMSR.KEY_DUKPT_MASTER);

                    textBuffer.append("ENCRYPTED MAGNETIC HEAD:");
                    textBuffer.append("\n");
                    textBuffer.append("Name: " + devInfo.name);
                    textBuffer.append("\n");
                    textBuffer.append("Serial: " + devInfo.serial);
                    textBuffer.append("\n");
                    textBuffer.append("Version: " + devInfo.version);
                    textBuffer.append("\n");
                    textBuffer.append("KEK Version: "
                            + (kekInfo.tampered ? "Tampered" : kekInfo.version));
                    textBuffer.append("\n");
                    textBuffer.append("AES Version: "
                            + (aesInfo.tampered ? "Tampered" : aesInfo.version));
                    textBuffer.append("\n");
                    textBuffer.append("DUKPT Version: "
                            + (desInfo.tampered ? "Tampered" : desInfo.version));
                }

                dialog(R.drawable.ic_info, getString(R.string.printer_info), textBuffer.toString());
            }
        }, R.string.title_read_information);
    }

    private void printSelfTest() {
        Log.d(LOG_TAG, "Print Self Test");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                printer.printSelfTest();
                printer.flush();
            }
        }, R.string.msg_printing_self_test);
    }

    private void printTextCopy() {
        Log.d(LOG_TAG, "Print Text");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                String textBuffer;
                //  textBuffer.append("{reset}{center}{w}{h}Yasha Kishi");
//                textBuffer.append("{br}");
//                textBuffer.append("{br}");
//                textBuffer.append("{reset}1. {b}First item{br}");
//                textBuffer.append("{reset}{right}{h}$0.50 A{br}");
//                textBuffer.append("{reset}2. {u}Second item{br}");
//                textBuffer.append("{reset}{right}{h}$1.00 B{br}");
//                textBuffer.append("{reset}3. {i}Third item{br}");
//                textBuffer.append("{reset}{right}{h}$1.50 C{br}");
//                textBuffer.append("{br}");
//                textBuffer.append("{reset}{right}{w}{h}TOTAL: {/w}$3.00  {br}");
//                textBuffer.append("{br}");
//                textBuffer.append("{reset}{center}{s}Thank You!{br}");


                printer.reset();

                printer.printTaggedText("{reset}{center}{b}");

                textBuffer = "شرکت آرمان پرداز آرنا";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{br}{reset}{right}{b}");


                textBuffer = "ارائه دهنده:";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{br}{right}");

                textBuffer = "1. چاپگرهاي حرارتي";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{s}{i}{right}");

                textBuffer = "2. صندوق هاي فروشگاهي هوشمند";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}------------------------{br}");


                textBuffer = "ا آ ب پ ت ث ج چ ح خ د ذ ر ز ژ س ش ص ض ط ظ ع غ ف ق ک گ ل م ن و ه ي  ِ  ْ ّ ُ ً َ ـ » « ، ؛ ؟ هٔ ي أ ؤ ئ ء ٪ ٫ ٬ 0 1 2 3 4 5 6 7 8 9 ي ي | ئ ئ  | ي ي )(";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{b}------------------------{br}");

                textBuffer = "آب است، ولي کم است)در مصرف آب صرفه جويي کنيم(";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}");

                textBuffer = " با استفاده از چابگرهاي حرارتي داتــــــکس قابل حمل تجربه سرويس دهي سريع را تجربه کنيد.";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{center}");

                textBuffer = "]5931-دي ماه[";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{b}************************{br}{center}{b}{u}");

                textBuffer = "با سپـــــــاس";
                printer.printArabicText(24, textBuffer);

                printer.printTaggedText("{br}{br}");

                printer.feedPaper(110);
                printer.flush();
            }
        }, R.string.msg_printing_text);
    }

    private void printText() {
        Log.d(LOG_TAG, "Print Text");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                DatecsWrapper wrapper = new DatecsWrapper(printer, DatecsWrapper.PrinterType.DPP450);

                printer.reset();

                List<DatecsStyle> l = new ArrayList<>();


                l.add(DatecsStyle.RESET);
                l.add(DatecsStyle.CENTER);
                l.add(DatecsStyle.BOLD);
                wrapper.setPrinterStyle(l);
                wrapper.printPersianText("شرکت پخش سایه سمن (سهامی خاص)");
                l.clear();


                l.add(DatecsStyle.BREAK);
                l.add(DatecsStyle.RESET);
                l.add(DatecsStyle.CENTER);
                l.add(DatecsStyle.BOLD);
                wrapper.setPrinterStyle(l);
                wrapper.printPersianText("نماینده انحصاری عالی فرد، شیوا، نستله و ردبول");
                l.clear();


                wrapper.setPrinterStyle("{br}{br}{reset}{center}{b}");
                wrapper.printPersianText("شماره درخواست: 101033");


                wrapper.setPrinterStyle("{reset}{br}{b}");
                wrapper.printPersianText("----------------------------------------------------");
                wrapper.setPrinterStyle("{br}");


                wrapper.printPersianText("آدرس دفتر و پخش مستقیم هورکا");


                wrapper.setPrinterStyle("{reset}{br}{s}{right}");

                wrapper.printPersianText("کیلومتر 20 جاده قدیم کرج-شهر قدس-بلوار انقلاب-خیابان شهید فصیحی- روبروی اداره کل آموزش و پرورش-شرکت پخش سایه سمن");

                wrapper.setPrinterStyle("{reset}{br}{s}{right}");

                wrapper.printPersianText("تلفن: (021)46821022، 10-04722272233");

                wrapper.setPrinterStyle("{reset}{br}{s}{right}");

                wrapper.printPersianText("فاکس:(021)46821022");

                wrapper.setPrinterStyle("{reset}{br}{right}{b}");

                ////////
                wrapper.printPersianText("____________________________________________________");

                wrapper.setPrinterStyle("{reset}{br}{right}");


                wrapper.printPersianText("شماره اقتصادی: 2545441022");

                wrapper.setPrinterStyle("{br}{right}");


                wrapper.printPersianText("شماره ثبت/شماره ملی: 546821022");

                wrapper.setPrinterStyle("{br}{right}");


                wrapper.printPersianText("کد پستی 10 رقمی: 46821022");

                wrapper.setPrinterStyle("{br}{right}");

                wrapper.printPersianText("شناسه ملی: 46821022");

                wrapper.setPrinterStyle("{br}{right}{b}");

                wrapper.printPersianText("____________________________________________________");

                wrapper.setPrinterStyle("{reset}{br}{right}{s}");


                wrapper.printPersianText("آدرس دفتر مرکزی:تهران میدان فردوسی، ابتدای خیابان سپهبد قرنی، کوچه علوی 2، پلاک 2");

                wrapper.setPrinterStyle("{br}{right}");

                wrapper.printPersianText("تلفن: 4682102288(تلفن شکایات و پیشنهادات: 889917496)");

                wrapper.setPrinterStyle("{reset}{br}");
                wrapper.printPersianText("____________________________________________________");
                wrapper.setPrinterStyle("{br}{right}");

                wrapper.printPersianText("تاریخ ویزیت: 1395/07/06      نوع پرداخت: نقد فقط");

                wrapper.setPrinterStyle("{br}{right}");

                wrapper.printPersianText("نام خریدار: پور صنعتی، یعقوبی (مسجد پیامبر اعظم کد خریدار: 2011574)");

                wrapper.setPrinterStyle("{br}{right}");

                wrapper.printPersianText("نشانی یا آدرس: سعادت آباد، شهرک مخابران، جنب مرکز خرید پرواز");

                wrapper.setPrinterStyle("{br}{b}");
                wrapper.printPersianText("----------------------------------------------------");
                wrapper.setPrinterStyle("{reset}{br}{right}{s}");


                wrapper.printPersianText("ردیف     شرح کالا      تعداد    بهای کل    تخفیفات     اضافات");

                wrapper.setPrinterStyle("{reset}{br}{b}");
                wrapper.printPersianText("----------------------------------------------------");
                wrapper.setPrinterStyle("{reset}{br}{right}{s}");


                wrapper.printPersianText("1 || شربت پرتقال13      2 || 139538  || 0(0.0%)  || 125326");


                wrapper.setPrinterStyle("{reset}{br}{b}");
                wrapper.printPersianText("----------------------------------------------------");
                wrapper.setPrinterStyle("{reset}{br}{right}{s}");


                wrapper.printPersianText("2 || شربت پرتقال13      2 || 139538  || 0(0.0%)  || 125326");


                wrapper.setPrinterStyle("{reset}{br}{b}");
                wrapper.printPersianText("====================================================");
                wrapper.setPrinterStyle("{reset}{br}{right}");


                wrapper.printPersianText("جمع کل: 2785.56  جمع اضافات: 250،625  جمع تخفیفات=0");

                wrapper.setPrinterStyle("{br}{right}");


                wrapper.printPersianText("اضافه مالیات ارزش افزوده: 168102  اضافه عوارض شهرداری: 83550.5");

                wrapper.setPrinterStyle("{br}{right}");

                wrapper.printPersianText("مجموع مبلغ ضایعات: 0");

                wrapper.setPrinterStyle("{br}{LEFT}");

                wrapper.printPersianText("مجموع مبلغ قابل پرداخت: 203،5807");


                printer.feedPaper(100);
                printer.flush();

            }
        }, R.string.msg_printing_text);
    }

    private void printText2() {
        runTask(new PrinterRunnable() {
                    @Override
                    public void run(ProgressDialog dialog, Printer printer) throws IOException {
                        DatecsWrapper wrapper = new DatecsWrapper(printer, DatecsWrapper.PrinterType.DPP450);
                        String textBuffer = new String();

                        printer.reset();

                        List<String> testStrings = new ArrayList<String>();
                        // sample text from http://www.hamyarit.com and http://uut.ac.ir and http://seefa.ir and http://118.tct.ir
                        testStrings.add("امروزه افراد زیادی از واژه پرداز محبوب شرکت مایکروسافت یعنی “واژه پرداز مایکروسافت ورد” استفاده میکنند، این نرم\u200Cافزار در میان کاربران فارسی زبان نیز از اهمیت ویژه\u200Cای برخوردار است و خیلی از کاربران ویندوز اعم از کاربران حرفه\u200Cای و آماتور از این برنامه برای ایجاد و ویرایش اسناد متنی خود استفاده میکند، این ابزار با امکانات و ویژگی\u200Cهای منحصر به فرد خود کاربران زیادی را جذب کرده است و امکان ویرایش متون به اکثر زبان\u200Cهای رایج دنیا در آن وجود دارد که زبان فارسی نیز یکی از آن\u200Cهاست، در ادامه با همیار آی\u200Cتی همراه باشید تا با هم نحوه نگارش صحیح جملات فارسی را در نرم\u200Cافزار مایکروسافت ورد مرور کنیم.");
                        testStrings.add("توجه: این کار تمام جملات شما را به صورت راست\u200Cچین نمایش میدهد، برای ارائه یک متن مرتب بهتر است Alignment را در حالت Justify قرار دهید.");
                        testStrings.add("یکی از راه\u200Cهای تنظیم فونت استفاده از منوی Font در تب Home میباشد ولی اگر بخواهید فونت خاصی را به کل نوشته اختصاص دهید (به صورت مجزا برای کلمات فارسی و انگلیسی) باید طبق روش زیر عمل کنید.\n" +
                                "مطابق تصویر زیر بر روی علامت کوچکی که در گوشه\u200Cی منوی Font قرار دارد کلیک کنید.");
                        testStrings.add("شما میتوانید با هر نوع فونتی که تمایل داشته باشید متن خود را بنویسید ولی اگر از یک فونت مناسب که برای زبان فارسی بهینه شده استفاده کنید زیبایی و خوانایی متن شما دو چندان خواهد شد، پس همواره این نکته را به یاد داشته باشید، معمولا فونت\u200Cهایی که با حروفی مانند B- یا IR- و Fa- آغاز میشوند مناسب نگارش فارسی هستند (البته استثنائاتی نیز در اینجا وجود دارد)");
                        testStrings.add("سعی کنید همیشه و در محل مناسب از علائم نگارشی استفاده کنید تا متن شما راحت\u200Cتر قابل خواندن باشد، اجزای اصلی علائم نگارشی در زبان پارسی شامل [\u200C؟\u200C!\u200C. ،: ؛ ” ” () … ] میشوند، البته نحوه استفاده از آن\u200Cها نیز اصول خاصی دارد که در ادامه به آن\u200Cها می\u200Cپردازیم.");
                        testStrings.add("پس از اینکه جمله\u200Cی مورد نظر خود را نوشتید در انتهای جمله و بدون درج هیچ\u200Cگونه فاصله\u200Cای علامت مناسب را قرار داده و سپس یک فاصله (Space) درج کنید.\n" +
                                "الگوی صحیح استفاده از این علائم به این صورت است:\n" +
                                "… [عبارت] [علائم نگارشی] [فاصله] [عبارت] …");
                        testStrings.add("شیوه نادرست:\n" +
                                "عبارت قبل( عبارت میان پرانتز  )عبارت بعد\n" +
                                "[  عبارت داخل کروشه  ]عبارت بعد\n" +
                                "عبارت”  داخل گیومه  “");

                        testStrings.add("شیوه صحیح:\n" +
                                "عبارت قبل (عبارت میان پرانتز) عبارت بعد\n" +
                                "[عبارت داخل کروشه] عبارت بعد\n" +
                                "عبارت “داخل گیومه”");

                        testStrings.add("استفاده از علائم ساده\u200Cی ریاضی\n" +
                                "یکی دیگر از نشانه\u200Cهایی که به استفاده از آن\u200Cها احتیاج پیدا خواهید کرد علامت\u200Cهای ساده\u200Cی ریاضی مانند: + – *  / ٪ هستند، به یاد داشته باشید علامت درصد باید به عدد قبلی خود چسبیده باشد (بدون فاصله) ولی قبل و بعد از علامت\u200Cهای +، – و … فاصله ایجاد کنید.\n" +
                                "نحوه\u200Cی صحیح به این صورت است:\n" +
                                "۸۵٪\n" +
                                "۲ + ۳\n" +
                                "Shift + Enter\n" +
                                "رعایت نکات بالا باعث میشود جملات تایپ شده در رایانه به صورت صحیح در تمام صفحات نمایش داده شوند (چراکه کامپیوتر با هر فاصله\u200Cای کلمه\u200Cی قبل و بعد را دو کلمه\u200Cی واحد و مجزا به حساب می\u200Cآورد و این امکان وجود دارد که بخشی از کلمه یا علامت آن به سطر بعدی منتقل شود، همچنین توجه داشته باشید که نیم\u200Cفاصله نیز به همین صورت عمل میکند، اگر با نیم فاصله آشنا نیستید این آموزش را ببینید: آموزش تصویری درج نیم\u200Cفاصله در مایکروسافت ورد)");

                        testStrings.add("برای وارد كردن اعداد كسری مانند ¼، ½ و ¾ كه در كیبوردهای كامپیوتر دیده نمی شوند می توان به ترتیب كدهای Alt0188, Alt0189, Alt0190 را به كار برد. برای تایپ اعداد حتماً باید از قسمت Numeric Pad كه در سمت راست كیبورد قرار گرفته است استفاده شود.");
                        testStrings.add("13) جابجایی متن: با ترفند زیر به راه حلی راحت و سریع برای كپی و یا جابجایی قسمتی از متن دست خواهید یافت: ابتدا متن و یا گرافیكی را كه تصمیم به حركت آن دارید علامت گذاری كنید. صفحه تصویر را با كمك حاشیه\u200Cهای آن كه در سمت راست و گوشه قابل حركتند. به قدری جابجا كنید كه مكانی را كه می\u200Cخواهید عنصر مربوطه به آن اضافه شود كاملا در دید قرار بگیرد. سپس برای جابجایی دكمه <Ctrl> و برای كپی كردن تركیب دكمه\u200Cهای <Ctrl>-<Shift> را كلیك كرده و با دكمه راست موش بر روی مقصد كلیك كنید.");
                        testStrings.add("15) بایگانی فایل : اینكه نرم\u200Cافزار Word چه پوشه\u200Cای را به صورت استاندارد در زمان استفاده از گزینه\u200Cهای File->Open یا File->Save نشان می\u200Cدهد می\u200Cتوانید شخصا تعیین كنید تنظیمات مربوطه را می\u200Cتوانید در Tools->Options->File locations->Documents بیابید. اینكه فایل\u200Cهایی را كه شخصا درست كرده و به صورت الگو در آورده\u200Cاید در كجا ثبت می\u200Cشوند، نیز در همان بخش، قسمت User templates قابل مشاهده\u200Cاند. نرم\u200Cافزار Word هنگامی كه شما برای ذخیره نوع فایلی DOT را انتخاب كنید از این دایركتوری استفاده می\u200Cكند.\n" +
                                "توسط دكمه Modify در همان بخش می\u200Cتوانید تنظیمات مربوطه را تغییر داده و تعیین كنیدكه برای مثال\u200C فایل\u200Cهای DOT و DOC در كجا ذخیره شوند. \n");
                        testStrings.add(" نوشته شده توسط تيم توسعه سيفا\n" +
                                " دسته: main-contents\n" +
                                " منتشر شده در 05 شهریور 1394\n" +
                                " بازدید: 1145");
                        testStrings.add("جهت ارائه هرگونه پیشنهادات و انتقادات در مورد این سایت و کدهای خدماتی  118 - 20119 - 191 - 192 - 20126   میتوانید با شماره تلفن 20196 تماس بگیرید .");
                        testStrings.add("1. تاریخ را به فرمت تاریخ میلادی بنویسیم ولی در قسمت format cell>custom فرمت ان را به صورت yy/mm/ddنوشت در این حالت تاریخ 21/05/1987 به صورت 21/05/87دیده میشوددراین حالت تاریخها به راحتی مقایسه میشوند و فرمت اطلاعات هم تاریخ هست و لی از نظر محاسبات روی تاریخ مشکل داریم مثلا ما تاریخ 31/03/87در تاریخ شمسی داریم ولی در تاریخ میلادی این تاریخ وجود ندارد چون ماه 3 انها 31 روزه نمیباشد و مشکل دوم اگر از توابع فاصله بین 2 تاریخ استفاده کنیم جواب با خطائ روبرو میشود.");
                        testStrings.add("یک باگ جالب در مورد فرمول dayweek هست. این فرمول قرار است روز هفته یک تاریخ مشخص را برگرداند. طبق بررسی من، از امروز به عقب تا تاریخ 1380/10/11 (سه شنبه) این فرمول درست کار می کند ولی روز قبلش را اشتباه نشان می دهد");
                        testStrings.add("شماره درخواست: 101033");
                        testStrings.add("کیلومتر 20 جاده قدیم کرج-شهر قدس-بلوار انقلاب-خ رازی-پ 40-کدپستی 6816877889");
                        testStrings.add("تلفن: 021-46467878 ، 021-98743200");
                        testStrings.add("فکس: 021-09097676");
                        testStrings.add("شماره ثبت/شماره ملی: 115361");
                        testStrings.add("کد پستی 10 رقمی: 1399837711");
                        testStrings.add("تلفن:88800892(تلفن شکایات و پیشنهادات: 88917049)");
                        testStrings.add("تاریخ ویزیت: 1395/07/06              نوع پرداخت : نقدی");
                        testStrings.add("نام خریدار: پوراحمدی، (مسجد سجاد) کد خریدار: 2011574");
                        testStrings.add("نام فروشنده: 20725- بهمن حسنی");
                        testStrings.add("ردیف |     شرح کالا      |    تعداد   |   بهای کل    |   تخفیفات | اضافات |");
                        testStrings.add("1  |   شربت پرتقال14   |   2   |  1392528  |  0)0.0(  | 125326 |");
                        testStrings.add("============================================================================");
                        testStrings.add("جمع کل: 278560056 جمع اضافات: 250652 جمع تخفیفات: 0.0");
                        testStrings.add("2 || شربت پرتقال13      2 || 139538  || 0(12.346%)  || 125326");
                        StringBuffer sb = new StringBuffer();
                        for (String s :
                                testStrings
                                ) {
                            sb.append(s);
                        }

                        wrapper.printPersianText(sb.toString());

                        printer.feedPaper(20);
                        printer.flush();

                    }
                }

                , R.string.msg_printing_text);
    }

    private void printText1() {
        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                DatecsWrapper wrapper = new DatecsWrapper(printer, DatecsWrapper.PrinterType.DPP250);
                String textBuffer = new String();

                printer.reset();
                List<DatecsStyle> l = new ArrayList<>();

                textBuffer = "آدرس دفتر و پخش مستقیم هورکا";
                l.add(DatecsStyle.BOLD);
                wrapper.setPrinterStyle(l);
                wrapper.printPersianText(textBuffer);
                l.clear();

                textBuffer = "آدرس دفتر و پخش مستقیم هورکا";
                wrapper.setPrinterStyle("{br}{s}");
                wrapper.setPrinterStyle(l);
                wrapper.printPersianText(textBuffer);

                printer.feedPaper(70);
                printer.flush();

            }
        }, R.string.msg_printing_text);
    }

    private void printCustomText(final String text) {
        Log.d(LOG_TAG, "Print Text");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                DatecsWrapper wrapper = new DatecsWrapper(printer, DatecsWrapper.PrinterType.DPP450);

                printer.reset();
                wrapper.printPersianText(text);
                printer.feedPaper(110);
                printer.flush();
            }
        }, R.string.msg_printing_text);
    }

    private void printCustomText1(final List<String> list) {
        Log.d(LOG_TAG, "Print Text");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                printer.reset();
                for (String s : list) {
                    printer.printArabicText(24, s);
                    printer.printTaggedText("{br}");
                    printer.feedPaper(10);
                }
                printer.flush();
            }
        }, R.string.msg_printing_text);
    }

    private void printImage() {
        Log.d(LOG_TAG, "Print Image");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;

                final AssetManager assetManager = getApplicationContext().getAssets();
                final Bitmap bitmap = BitmapFactory.decodeStream(assetManager.open("sample.png"),
                        null, options);
                final int width = bitmap.getWidth();
                final int height = bitmap.getHeight();
                final int[] argb = new int[width * height];
                bitmap.getPixels(argb, 0, width, 0, 0, width, height);
                bitmap.recycle();

                printer.reset();
                printer.printCompressedImage(argb, width, height, Printer.ALIGN_CENTER, true);
                printer.feedPaper(110);
                printer.flush();
            }
        }, R.string.msg_printing_image);
    }

    private void printPage() {
        Log.d(LOG_TAG, "Print Page");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                PrinterInformation pi = printer.getInformation();

                if (!pi.isPageSupported()) {
                    dialog(R.drawable.ic_page, getString(R.string.title_warning),
                            getString(R.string.msg_unsupport_page_mode));
                    return;
                }

                printer.reset();
                printer.selectPageMode();

                printer.setPageRegion(0, 0, 160, 320, Printer.PAGE_LEFT);
                printer.setPageXY(0, 4);
                printer.printTaggedText("{reset}{center}{b}PARAGRAPH I{br}");
                printer.drawPageRectangle(0, 0, 160, 32, Printer.FILL_INVERTED);
                printer.setPageXY(0, 34);
                printer.printTaggedText("{reset}Text printed from left to right"
                        + ", feed to bottom. Starting point in left top corner of the page.{br}");
                printer.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                printer.setPageRegion(160, 0, 160, 320, Printer.PAGE_TOP);
                printer.setPageXY(0, 4);
                printer.printTaggedText("{reset}{center}{b}PARAGRAPH II{br}");
                printer.drawPageRectangle(160 - 32, 0, 32, 320, Printer.FILL_INVERTED);
                printer.setPageXY(0, 34);
                printer.printTaggedText("{reset}Text printed from top to bottom"
                        + ", feed to left. Starting point in right top corner of the page.{br}");
                printer.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                printer.setPageRegion(160, 320, 160, 320, Printer.PAGE_RIGHT);
                printer.setPageXY(0, 4);
                printer.printTaggedText("{reset}{center}{b}PARAGRAPH III{br}");
                printer.drawPageRectangle(0, 320 - 32, 160, 32, Printer.FILL_INVERTED);
                printer.setPageXY(0, 34);
                printer.printTaggedText("{reset}Text printed from right to left"
                        + ", feed to top. Starting point in right bottom corner of the page.{br}");
                printer.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                printer.setPageRegion(0, 320, 160, 320, Printer.PAGE_BOTTOM);
                printer.setPageXY(0, 4);
                printer.printTaggedText("{reset}{center}{b}PARAGRAPH IV{br}");
                printer.drawPageRectangle(0, 0, 32, 320, Printer.FILL_INVERTED);
                printer.setPageXY(0, 34);
                printer.printTaggedText("{reset}Text printed from bottom to top"
                        + ", feed to right. Starting point in left bottom corner of the page.{br}");
                printer.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                printer.printPage();
                printer.selectStandardMode();
                printer.feedPaper(110);
                printer.flush();
            }
        }, R.string.msg_printing_page);
    }

    private void printBarcode() {
        Log.d(LOG_TAG, "Print Barcode");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                printer.reset();

                printer.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_BELOW, 100);
                printer.printBarcode(Printer.BARCODE_CODE128AUTO, "123456789012345678901234");
                printer.feedPaper(38);

                printer.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_BELOW, 100);
                printer.printBarcode(Printer.BARCODE_EAN13, "123456789012");
                printer.feedPaper(38);

                printer.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_BOTH, 100);
                printer.printBarcode(Printer.BARCODE_CODE128, "ABCDEF123456");
                printer.feedPaper(38);

                printer.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_NONE, 100);
                printer.printBarcode(Printer.BARCODE_PDF417, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                printer.feedPaper(38);

                printer.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_NONE, 100);
                printer.printQRCode(4, 3, "http://www.datecs.bg");
                printer.feedPaper(38);

                printer.feedPaper(110);
                printer.flush();
            }
        }, R.string.msg_printing_barcode);
    }

    private void readCard() {
        Log.d(LOG_TAG, "Read card");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                PrinterInformation pi = printer.getInformation();
                String[] tracks = null;
                FinancialCard card = null;
                Printer.setDebug(true);
                if (pi.getName().startsWith("CMP-10")) {
                    // The printer CMP-10 can read only two tracks at once.
                    tracks = printer.readCard(true, true, false, 15000);
                } else {
                    tracks = printer.readCard(true, true, true, 15000);
                }

                if (tracks != null) {
                    StringBuffer textBuffer = new StringBuffer();

                    if (tracks[0] == null && tracks[1] == null && tracks[2] == null) {
                        textBuffer.append(getString(R.string.no_card_read));
                    } else {
                        if (tracks[0] != null) {
                            card = new FinancialCard(tracks[0]);
                        } else if (tracks[1] != null) {
                            card = new FinancialCard(tracks[1]);
                        }

                        if (card != null) {
                            textBuffer.append(getString(R.string.card_no) + ": " + card.getNumber());
                            textBuffer.append("\n");
                            textBuffer.append(getString(R.string.holder) + ": " + card.getName());
                            textBuffer.append("\n");
                            textBuffer.append(getString(R.string.exp_date)
                                    + ": "
                                    + String.format("%02d/%02d", card.getExpiryMonth(),
                                    card.getExpiryYear()));
                            textBuffer.append("\n");
                        }

                        if (tracks[0] != null) {
                            textBuffer.append("\n");
                            textBuffer.append(tracks[0]);

                        }
                        if (tracks[1] != null) {
                            textBuffer.append("\n");
                            textBuffer.append(tracks[1]);
                        }
                        if (tracks[2] != null) {
                            textBuffer.append("\n");
                            textBuffer.append(tracks[2]);
                        }
                    }

                    dialog(R.drawable.ic_card, getString(R.string.card_info), textBuffer.toString());
                }
            }
        }, R.string.msg_reading_magstripe);
    }

    private void readCardEncrypted() {
        Log.d(LOG_TAG, "Read card encrypted");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                byte[] buffer = mEMSR.readCardData(EMSR.MODE_READ_TRACK1 | EMSR.MODE_READ_TRACK2
                        | EMSR.MODE_READ_TRACK3 | EMSR.MODE_READ_PREFIX);
                StringBuffer textBuffer = new StringBuffer();

                int encryptionType = (buffer[0] >>> 3);
                // Trim extract encrypted block.
                byte[] encryptedData = new byte[buffer.length - 1];
                System.arraycopy(buffer, 1, encryptedData, 0, encryptedData.length);

                if (encryptionType == EMSR.ENCRYPTION_TYPE_OLD_RSA
                        || encryptionType == EMSR.ENCRYPTION_TYPE_RSA) {
                    try {
                        String[] result = CryptographyHelper.decryptTrackDataRSA(encryptedData);
                        textBuffer.append("Track2: " + result[0]);
                        textBuffer.append("\n");
                    } catch (Exception e) {
                        error("Failed to decrypt RSA data: " + e.getMessage());
                        return;
                    }
                } else if (encryptionType == EMSR.ENCRYPTION_TYPE_AES256) {
                    try {
                        String[] result = CryptographyHelper.decryptAESBlock(encryptedData);

                        textBuffer.append("Random data: " + result[0]);
                        textBuffer.append("\n");
                        textBuffer.append("Serial number: " + result[1]);
                        textBuffer.append("\n");
                        if (result[2] != null) {
                            textBuffer.append("Track1: " + result[2]);
                            textBuffer.append("\n");
                        }
                        if (result[3] != null) {
                            textBuffer.append("Track2: " + result[3]);
                            textBuffer.append("\n");
                        }
                        if (result[4] != null) {
                            textBuffer.append("Track3: " + result[4]);
                            textBuffer.append("\n");
                        }
                    } catch (Exception e) {
                        error("Failed to decrypt AES data: " + e.getMessage());
                        return;
                    }
                } else if (encryptionType == EMSR.ENCRYPTION_TYPE_IDTECH) {
                    try {
                        String[] result = CryptographyHelper.decryptIDTECHBlock(encryptedData);

                        textBuffer.append("Card type: " + result[0]);
                        textBuffer.append("\n");
                        if (result[1] != null) {
                            textBuffer.append("Track1: " + result[1]);
                            textBuffer.append("\n");
                        }
                        if (result[2] != null) {
                            textBuffer.append("Track2: " + result[2]);
                            textBuffer.append("\n");
                        }
                        if (result[3] != null) {
                            textBuffer.append("Track3: " + result[3]);
                            textBuffer.append("\n");
                        }
                    } catch (Exception e) {
                        error("Failed to decrypt IDTECH data: " + e.getMessage());
                        return;
                    }
                } else {
                    textBuffer.append("Encrypted block: " + HexUtil.byteArrayToHexString(buffer));
                    textBuffer.append("\n");
                }

                dialog(R.drawable.ic_card, getString(R.string.card_info), textBuffer.toString());
            }
        }, R.string.msg_reading_magstripe);
    }

    private void readBarcode(final int timeout) {
        Log.d(LOG_TAG, "Read Barcode");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                String barcode = printer.readBarcode(timeout);

                if (barcode != null) {
                    dialog(R.drawable.ic_read_barcode, getString(R.string.barcode), barcode);
                }
            }
        }, R.string.msg_reading_barcode);
    }

    private void enrolNewIdentity(final String identity) {
        Log.d(LOG_TAG, "Enrol new identity");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                TouchChip tc = printer.getTouchChip();

                try {
                    tc.enrolIdentity(identity);
                } catch (TouchChipException e) {
                    error("Failed to enrol identity: " + e.getMessage());
                }

            }
        }, R.string.msg_enrol_identity);
    }

    private void deleteAllIdentities() {
        Log.d(LOG_TAG, "Delete all identities");

        runTask(new PrinterRunnable() {
            @Override
            public void run(ProgressDialog dialog, Printer printer) throws IOException {
                final TouchChip tc = printer.getTouchChip();

                try {
                    int[] slots = tc.listSlots();

                    for (int slot : slots) {
                        tc.deleteIdentity(slot);
                    }
                } catch (TouchChipException e) {
                    error("Failed to delete fingerprints: " + e.getMessage());
                }

            }
        }, R.string.msg_delete_all_identities);
    }

    private void checkIdentity() {
        Log.d(LOG_TAG, "Check identity");

        runTask(new PrinterRunnable() {
            @Override
            public void run(final ProgressDialog dialog, Printer printer) throws IOException {
                final TouchChip tc = printer.getTouchChip();

                final Identity identity;
                try {
                    identity = tc.checkIdentity();

                } catch (TouchChipException e) {
                    error("Failed to check identity: " + e.getMessage());
                    return;
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        FingerprintView v = (FingerprintView) findViewById(R.id.fingerprint);
                        v.setText(identity.getIdentityAsString());
                    }
                });
            }
        }, R.string.msg_check_identity);
    }

    private void getIdentity() {
        Log.d(LOG_TAG, "Get identity");

        runTask(new PrinterRunnable() {
            @Override
            public void run(final ProgressDialog dialog, Printer printer) throws IOException {
                final TouchChip tc = printer.getTouchChip();

                final AnsiIso ansiIso;
                try {
                    ImageReceiver receiver = new ImageReceiver() {
                        @Override
                        public void onDataReceived(int totalSize, int bytesRecv, byte[] data) {
                            final String message = getString(R.string.msg_downloading_image) + (100 * bytesRecv / totalSize) + "%";

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    dialog.setMessage(message);
                                }
                            });
                        }
                    };
                    ansiIso = tc.getIdentity(TouchChip.IMAGE_SIZE_SMALL, TouchChip.IMAGE_FORMAT_ISO, TouchChip.IMAGE_COMPRESSION_NONE, receiver);
                } catch (TouchChipException e) {
                    error("Failed to get identity: " + e.getMessage());
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("General Header: " + ansiIso.getHeaderAsHexString());
                        System.out.println("Format identifier: " + ansiIso.getFormatIdentifierAsString());
                        System.out.println("Version number: " + ansiIso.getVersionNumberAsString());
                        System.out.println("Record length: " + ansiIso.getRecordLength());
                        System.out.println("CBEFF Product Identifier: " + ansiIso.getProductIdentifierAsHexString());
                        System.out.println("Capture device ID: " + ansiIso.getCaptureDeviceIDAsHexString());
                        System.out.println("Number of fingers/palms: " + ansiIso.getNumberOfFingers());
                        System.out.println("Scale Units: " + ansiIso.getScaleUnits());
                        System.out.println("Scan resolution (horiz): " + ansiIso.getScanResolutionHorz());
                        System.out.println("Scan resolution (vert): " + ansiIso.getScanResolutionVert());
                        System.out.println("Image resolution (horiz): " + ansiIso.getImageResolutionHorz());
                        System.out.println("Image resolution (vert): " + ansiIso.getImageResolutionVert());
                        System.out.println("Pixel depth: " + ansiIso.getPixelDepth());
                        System.out.println("Image compression algorithm: " + ansiIso.getImageCompressionAlgorithm());
                        System.out.println("Length of finger data block: " + ansiIso.getFingerDataBlockLength());
                        System.out.println("Finger/palm position: " + ansiIso.getFingerPosition());
                        System.out.println("Count of views: " + ansiIso.getCountOfViews());
                        System.out.println("View number: " + ansiIso.getViewNumber());
                        System.out.println("Finger/palm image quality: " + ansiIso.getFingerImageQuality());
                        System.out.println("Impression type: " + ansiIso.getImpressionType());
                        System.out.println("Horizontal line length: " + ansiIso.getHorizontalLineLength());
                        System.out.println("Vertical line length: " + ansiIso.getVerticalLineLength());

                        FingerprintView v = (FingerprintView) findViewById(R.id.fingerprint);
                        v.setImage(ansiIso.getHorizontalLineLength(), ansiIso.getVerticalLineLength(), ansiIso.getImageData());
                    }
                });
            }
        }, R.string.msg_get_identity);
    }

    private void processContactlessCard(ContactlessCard contactlessCard) {
        final StringBuffer msgBuf = new StringBuffer();

        if (contactlessCard instanceof ISO14443Card) {
            ISO14443Card card = (ISO14443Card) contactlessCard;
            msgBuf.append("ISO14 card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("ISO14 type: " + card.type + "\n");

            if (card.type == ContactlessCard.CARD_MIFARE_DESFIRE) {
                ProtocolAdapter.setDebug(true);
                mPrinterChannel.suspend();
                mUniversalChannel.suspend();
                try {

                    card.getATS();
                    Log.d(LOG_TAG, "Select application");
                    card.DESFire().selectApplication(0x78E127);
                    Log.d(LOG_TAG, "Application is selected");
                    msgBuf.append("DESFire Application: " + Integer.toHexString(0x78E127) + "\n");
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Select application", e);
                } finally {
                    ProtocolAdapter.setDebug(false);
                    mPrinterChannel.resume();
                    mUniversalChannel.resume();
                }
            }
            /*
             // 16 bytes reading and 16 bytes writing
             // Try to authenticate first with default key
            byte[] key= new byte[] {-1, -1, -1, -1, -1, -1};
            // It is best to store the keys you are going to use once in the device memory, 
            // then use AuthByLoadedKey function to authenticate blocks rather than having the key in your program
            card.authenticate('A', 8, key);
            
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };            
            card.write16(8, input);
            
            // Read data from card
            byte[] result = card.read16(8);
            */
        } else if (contactlessCard instanceof ISO15693Card) {
            ISO15693Card card = (ISO15693Card) contactlessCard;

            msgBuf.append("ISO15 card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("Block size: " + card.blockSize + "\n");
            msgBuf.append("Max blocks: " + card.maxBlocks + "\n");

            /*
            if (card.blockSize > 0) {
                byte[] security = card.getBlocksSecurityStatus(0, 16);
                ...
                
                // Write data to the card
                byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03 };
                card.write(0, input);
                ...
                
                // Read data from card
                byte[] result = card.read(0, 1); 
                ...
            }
            */
        } else if (contactlessCard instanceof FeliCaCard) {
            FeliCaCard card = (FeliCaCard) contactlessCard;

            msgBuf.append("FeliCa card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
               
            /*
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };            
            card.write(0x0900, 0, input);
            ...
            
            // Read data from card
            byte[] result = card.read(0x0900, 0, 1);
            ...
            */
        } else if (contactlessCard instanceof STSRICard) {
            STSRICard card = (STSRICard) contactlessCard;

            msgBuf.append("STSRI card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("Block size: " + card.blockSize + "\n");

            /*
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03 };
            card.writeBlock(8, input);
            ...
            
            // Try reading two blocks
            byte[] result = card.readBlock(8);
            ...
            */
        } else {
            msgBuf.append("Contactless card: " + HexUtil.byteArrayToHexString(contactlessCard.uid));
        }

        dialog(R.drawable.ic_tag, getString(R.string.tag_info), msgBuf.toString());

        // Wait silently to remove card 
        try {
            contactlessCard.waitRemove();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
