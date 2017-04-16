package com.digitalpersona.onetouch.huellasclientes;

import Controller.HuellasController;

/**
 * Abstract user interface
 */
public interface UserInterface extends Runnable {

    /**
     * User interface factory
     */
    public static interface Factory {
        
        UserInterface createUI(HuellasController util);
    }
}
