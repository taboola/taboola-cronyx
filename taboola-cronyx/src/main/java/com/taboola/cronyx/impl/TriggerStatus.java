package com.taboola.cronyx.impl;

public enum TriggerStatus {

    /*
    The trigger is active and is either being executed or waiting for its schedule
     */
    ACTIVE,

    /*
    The trigger has been paused, preventing any future executions
     */
    PAUSED,

    /*
    The trigger has ended with exception the last time it ran and has yet to recover from it
     */
    ERROR,

    /*
    The trigger has ended and will not be executed again in the future
     */
    COMPLETE
}
