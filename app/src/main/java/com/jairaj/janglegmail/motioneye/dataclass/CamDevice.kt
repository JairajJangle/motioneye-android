package com.jairaj.janglegmail.motioneye.dataclass

class CamDevice(
    var label: String,
    var urlPort: String,
    var driveLink: String,

    var reorderHandleVisibility: Boolean? = null,
    var expandCollapseButtonVisibility: Boolean? = null,
    var previewVisibility: Boolean? = null,
    var checkBoxVisibility: Boolean? = null,
    var checkBoxIsChecked: Boolean? = null,
)