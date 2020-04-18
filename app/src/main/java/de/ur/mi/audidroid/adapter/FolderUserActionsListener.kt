package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.FolderEntity

interface FolderUserActionsListener {
    fun onAddFolderClicked(folderEntity: FolderEntity? = null, view: View)

    fun openFolderPopupMenu(folderEntity: FolderEntity, view: View)
}
