/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageDetailActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageSendActivity;
import cat.bcn.vincles.tablet.model.GroupModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GroupsChatDetailActivity extends TaskMessageDetailActivity {
    private final String TAG = this.getClass().getSimpleName();
    private GroupModel groupModel = GroupModel.getInstance();
    protected VinclesGroup vinclesGroup;

    protected String currentFilename;
    protected String currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_groups_chat_detail;
        super.onCreate(savedInstanceState);

        // CAUTION: initialize group action & private chat controls
        taskModel.isGroupAction = true;
        taskModel.isPrivateChat = false;
    }

    public void goClose (View v) {
        finish();
    }

    public void goGroups(View view) {
        startActivity(new Intent(this, GroupsListActivity.class));
        finish();
    }

    public void goDinamizerChat(View view) {
        Intent intent = new Intent(this, GroupsDinamizerChatActivity.class);
        startActivity(intent);
        finish();
    }

    public void takePhoto(View view) {

        // Indicate file uri to save
        currentFilename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
        File currentImageFile = new File(VinclesConstants.getImagePath(), currentFilename);
        currentImagePath = currentImageFile.getAbsolutePath();
        Uri currentImageUri = Uri.fromFile(currentImageFile);

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
        startActivityForResult(intent, VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        finish();
    }

    @Override
    public void goBack(View view) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);

        if (requestCode == VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Send high resolution image
                sendImage();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise userVincles
            }
        }
    }

    private void sendImage() {
        // Create chat with Current Resource to send
        taskModel.setCurrentChat(new Chat());
        taskModel.currentGroup = groupModel.currentGroup;
        taskModel.getCurrentChat().idUserFrom = mainModel.currentUser.getId();
        taskModel.getCurrentChat().idUserTo = taskModel.currentUser.getId();
        taskModel.getCurrentChat().resourceTempList = new ArrayList<Resource>();
        Resource resource = new Resource();
        resource.chat = taskModel.getCurrentChat();
        taskModel.getCurrentChat().metadataTipus = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
        taskModel.getCurrentChat().resourceTempList.add(resource);
        File imageFile = new File(currentImagePath);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        resource.filename = currentFilename;

        resource.data = MultipartBody.Part.createFormData("file", resource.filename, file);

        taskModel.getCurrentChat().resourceTempList.add(resource);
        taskModel.getCurrentChat().sendTime = new Date();

        List<Long> idChatList = new ArrayList<Long>();

        if (groupModel.currentGroup != null) {
            if (!taskModel.isPrivateChat) {
                taskModel.getCurrentChat().idChat = groupModel.currentGroup.idChat;
                idChatList.add(taskModel.currentGroup.idChat);
            } else {
                taskModel.getCurrentChat().idChat = groupModel.currentGroup.idDynamizerChat;
                idChatList.add(taskModel.currentGroup.idDynamizerChat);
            }
        }

        final ProgressDialog loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage(getString(R.string.general_download));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.setInverseBackgroundForced(true);
        loading.show();

        groupModel.sendChatToAll(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                loading.dismiss();
                getLocalData();
            }

            @Override
            public void onFailure(Object error) {
                Log.i(TAG, "sendImage().sendChatToAll() - error: " + error);
                loading.dismiss();
                String errorMessage = mainModel.getErrorByCode(error);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, taskModel.getCurrentChat(), idChatList);
    }

    public void getLocalData() {
        // Override
    }

    public void createChat(View view) {
        if (groupModel.currentGroup == null) return;
        Log.i(TAG, "createChat()");
        taskModel.currentGroup = groupModel.currentGroup;
        taskModel.setCurrentChat(new Chat());
        taskModel.getCurrentChat().idChat = taskModel.currentGroup.idChat;
        startActivity(new Intent(this, TaskMessageSendActivity.class));
        finish();
    }
}
