package com.digitalpersona.onetouch.huellasclientes;

import Controller.HuellasController;
import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPDataListener;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of UserInterface.Factory interface that creates a console-based user interface
 */
public class ConsoleUserInterfaceFactory implements UserInterface.Factory {

    /**
     * Creates an object implementing UserInterface interface
     *
     * @param util
     * @return created instance
     */
    @Override
    public UserInterface createUI(HuellasController util) {
        return new ConsoleUserInterface(util);
    }

    /**
     * Console-based UserInterface
     */
    private static class ConsoleUserInterface implements UserInterface {

        
        private final HuellasController util;

       
        public ConsoleUserInterface(HuellasController util) {
            
            this.util = util;
        }

       
        @Override
        public void run() {
            System.out.println("\n*** Manejador Portero 4.0 ***");
            
            boolean readerSelected = false;
            
            String activeReader = selectReader(null);
            if(activeReader!=null){
                    readerSelected = true;
                    verify(activeReader);
                    
            }

            System.out.println("readerSelected "+readerSelected);
        }

              
        /**
         * Acquires fingerprint from the sensor and matches it with the registration templates
         * stored in the database.
         *
         * @param activeReader fingerprint reader to use
         */
        private void verify(String activeReader) {
            System.out.printf("Proceso a verificar...\n");
            
                while(true){
                    try {
                        DPFPSample sample = getSample(activeReader,"Escaneo de huella \n");
                        if (sample == null){
                            System.out.println("aqui error");
                            throw new Exception();
                            
                        }

                        DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
                        DPFPFeatureSet featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                        DPFPVerification matcher = DPFPGlobal.getVerificationFactory().createVerification();
                        matcher.setFARRequested(DPFPVerification.MEDIUM_SECURITY_FAR);
                        
                                               
                        this.util.buscarHuella(featureSet, matcher,sample);
                        
                    } catch (Exception e) {
                        System.out.printf("Failed to perform verification.");
                    }
                }
                  
        }

       
        /**
         * selectReader() stores chosen reader in *pActiveReader
         * @param activeReader currently selected reader
         * @return reader selected
         * @throws IndexOutOfBoundsException if no reader available
         */
        public String selectReader(String activeReader)  {
            DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();
                 
            String lector=null;
                        
            if (readers == null || readers.isEmpty()){
                System.out.println("No hay lectores conectados disponibles");
            }
            else{
                lector=readers.get(0).getSerialNumber();
            }
            
            
            return lector;

          
        }

        /**
         * Acquires a fingerprint sample from the specified fingerprint reader
         *
         * @param activeReader Fingerprint reader to use for acquisition
         * @return sample acquired
         * @throws InterruptedException if thread is interrupted
         */
        private DPFPSample getSample(String activeReader, String prompt)
        	throws InterruptedException
        {
            final LinkedBlockingQueue<DPFPSample> samples = new LinkedBlockingQueue<>();
            DPFPCapture capture = DPFPGlobal.getCaptureFactory().createCapture();
            capture.setReaderSerialNumber(activeReader);
            capture.setPriority(DPFPCapturePriority.CAPTURE_PRIORITY_LOW);
//            capture.addDataListener(new DPFPDataListener()
              capture.addDataListener((DPFPDataEvent e) -> {
                  if (e != null && e.getSample() != null) {
                      try {
                          samples.put(e.getSample());
                      } catch (InterruptedException e1) {
                          System.out.println("e1"+e1);
                      }
                  }
            });
            capture.addReaderStatusListener(new DPFPReaderStatusAdapter()
            {
            	int lastStatus = DPFPReaderStatusEvent.READER_CONNECTED;
                    @Override
				public void readerConnected(DPFPReaderStatusEvent e) {
					if (lastStatus != e.getReaderStatus())
						System.out.println("Reader is connected");
					lastStatus = e.getReaderStatus();
				}
                    @Override
				public void readerDisconnected(DPFPReaderStatusEvent e) {
					if (lastStatus != e.getReaderStatus())
						System.out.println("Reader is disconnected");
					lastStatus = e.getReaderStatus();
				}
            	
            });
            try {
                capture.startCapture();
                System.out.print(prompt);
                return samples.take();
            } catch (RuntimeException e) {
                System.out.printf("Failed to start capture. Check that reader is not used by another application.\n");
                throw e;
            } finally {
                capture.stopCapture();
            }
        }

        
       
    }
}
