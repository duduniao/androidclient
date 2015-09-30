/*
 * Kontalk Android client
 * Copyright (C) 2014 Kontalk Devteam <devteam@kontalk.org>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kontalk.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;

import org.kontalk.R;
import org.kontalk.data.Contact;
import org.kontalk.data.Contact.ContactCallback;

public abstract class AvatarListItem extends RelativeLayout implements ContactCallback {

    protected QuickContactBadge mAvatarView;

    private Handler mHandler;

    static protected Drawable sDefaultContactImage;

    public AvatarListItem(Context context) {
        super(context);
        init(context);
    }

    public AvatarListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHandler = new Handler();

        if (sDefaultContactImage == null)
            sDefaultContactImage = context.getResources()
                .getDrawable(R.drawable.ic_contact_picture);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAvatarView = (QuickContactBadge) findViewById(R.id.avatar);

        if (isInEditMode()) {
            RoundImage r = new RoundImage(sDefaultContactImage);
            mAvatarView.setImageDrawable(r);
            mAvatarView.setVisibility(VISIBLE);
        }
    }

    protected void loadAvatar(Contact contact) {
        if (contact != null) {
            // we mark this with the contact's hash code for the async avatar
            mAvatarView.setTag(contact.hashCode());
            mAvatarView.assignContactUri(contact.getUri());
            RoundImage r = new RoundImage(sDefaultContactImage);
            mAvatarView.setImageDrawable(r);
            // laod avatar asynchronously
            contact.getAvatarAsync(getContext(), this);
        }
        else {
            mAvatarView.setTag(null);
            RoundImage r = new RoundImage(sDefaultContactImage);
            mAvatarView.setImageDrawable(r);
        }
    }

    @Override
    public void avatarLoaded(final Contact contact, final Drawable avatar) {
        if (avatar != null) {
            if (mHandler.getLooper().getThread() != Thread.currentThread()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        updateAvatar(contact, avatar);
                    }
                });
            }
            else {
                updateAvatar(contact, avatar);
            }
        }
    }

    private void updateAvatar(Contact contact, Drawable avatar) {
        try {
            // be sure the contact is still the same
            // this is an insane workaround against race conditions
            Integer contactTag = (Integer) mAvatarView.getTag();
            if (contactTag != null && contactTag.intValue() == contact.hashCode()) {
                RoundImage r = new RoundImage(avatar);
                mAvatarView.setImageDrawable(r);
            }
        }
        catch (Exception e) {
            // we are deliberately ignoring any exception here
            // because an error here could happen only if something
            // weird is happening, e.g. user leaving the activity
        }
    }

}