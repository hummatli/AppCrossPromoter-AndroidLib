package com.mobapphome.mahads.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;

import com.mobapphome.mahads.MAHAdsDlgExit;
import com.mobapphome.mahads.MAHAdsDlgPrograms;
import com.mobapphome.mahads.R;
import com.mobapphome.mahads.types.MAHRequestResult;
import com.mobapphome.mahads.types.Program;

import java.util.List;

public class MAHAdsController {
	public String urlForProgramVersion;
	public String urlForProgramList;
	public String urlRootOnServer;
	private boolean internalCalled = false;
	private String fontName = null;

	public MAHRequestResult mahRequestResult;
	private Updater updater;

	/**
	 * Initializes MAHAds library
	 * @param activity Activity which init calls
	 * @param urlRootOnServer Root of services which programs have listed. Inside of this method:
	 *                        urlForProgramVersion = urlRootOnServer + "program_version.php"
	 *                        urlForProgramList = urlRootOnServer + "program_list.php"
     */
	@Deprecated
	public void init(@NonNull final FragmentActivity activity,
					 @NonNull String urlRootOnServer) {
		init(activity, urlRootOnServer, "program_version.php", "program_list.php");
	}

	/**
	 * Initializes MAHAds library
	 * @param activity Activity which init calls
	 * @param urlRootOnServer Root of services which programs have listed.
	 * @param programVersionUrlEnd Url end for program version
	 * @param urlForProgramListUrlEnd Url end for program list
	 */
	public void init(@NonNull final FragmentActivity activity,
					 @NonNull String urlRootOnServer,
					 @NonNull String programVersionUrlEnd,
					 @NonNull String urlForProgramListUrlEnd) {
		init(activity, urlRootOnServer + programVersionUrlEnd, urlRootOnServer + urlForProgramListUrlEnd);
	}

	/**
	 * Initializes MAHAds library
	 * @param activity Activity which init calls
	 * @param urlForProgramVersion Url for program version
	 * @param urlForProgramList Url for program list. In this case: urlRootOnServer will be set to the root of urlForProgramList
	 */
	public void init(@NonNull final FragmentActivity activity,
					 @NonNull String urlForProgramVersion,
					 @NonNull String urlForProgramList) {
		this.urlForProgramVersion = urlForProgramVersion;
		this.urlForProgramList = urlForProgramList;
		this.urlRootOnServer = Utils.getRootFromUrl(urlForProgramList);

		setInternalCalled(activity.getIntent().getBooleanExtra(Constants.MAH_ADS_INTERNAL_CALLED, false));

		getUpdater().updateProgramList(activity);
	}

	/**
	 * Gets static updater object created in init method or creates new one if is null
	 * @return updater object
     */
	public Updater getUpdater() {
		if(updater == null){
			updater = new Updater(this);
		}
		return updater;
	}

	/**
	 * Calls ExitDialog to open. If current dialog has opened through MAHAds dialogs
	 * then application will quit not opening ExitDialog
	 * @param activity Activity which method has called
	 */
	public void callExitDialog(FragmentActivity activity) {
		callExitDialog(activity, true, true);
	}

	/**
	 * Calls ExitDialog to open. If current dialog has opened through MAHAds dialogs
	 * then application will quit not opening ExitDialog
	 * @param activity Activity which method has called
	 * @param btnInfoVisibility If true shows info button
	 * @param btnInfoWithMenu If true adds popup menu to info button
	 */
	public void callExitDialog(FragmentActivity activity,
							   boolean btnInfoVisibility,
							   boolean btnInfoWithMenu) {
		callExitDialog(activity, btnInfoVisibility, btnInfoWithMenu, activity.getString(R.string.mah_ads_info_popup_text), Constants.MAH_ADS_GITHUB_LINK);
	}

	/**
	 * Calls ExitDialog to open. If current dialog has opened through MAHAds dialogs
	 * then application will quit not opening ExitDialog
	 * @param activity Activity which method has called
	 * @param btnInfoVisibility If true shows info button
	 * @param btnInfoWithMenu If true adds popup menu to info button
	 * @param btnInfoMenuItemTitle Title of menu item for info button
	 * @param btnInfoActionURL Url to open when clicking to info button or info menu item
     */
	public void callExitDialog(FragmentActivity activity,
									  boolean btnInfoVisibility,
									  boolean btnInfoWithMenu,
									  String btnInfoMenuItemTitle,
									  @NonNull String btnInfoActionURL) {
		//When is internal call is true then exit dialog will not open.
		//It will be true only program opens through MAHAds components
		if(isInternalCalled()){
			// This makes sure that the container activity has implemented
			// the callback interface. If not, it throws an exception
			try {
				MAHAdsDlgExit.MAHAdsDlgExitListener exitCallback = (MAHAdsDlgExit.MAHAdsDlgExitListener) activity;
				exitCallback.onExitWithoutExitDlg();
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement MAHAdsDlgExitListener");
			}
		}else{
			showDlg(activity,
					MAHAdsDlgExit.newInstance(mahRequestResult, urlRootOnServer, fontName, btnInfoVisibility, btnInfoWithMenu, btnInfoMenuItemTitle, btnInfoActionURL),
					Constants.TAG_MAH_ADS_DLG_EXIT);
		}
	}

	/**
	 * Calls ProgramsDialog to open
	 * @param activity Activity which method has called
     */
	public void callProgramsDialog(FragmentActivity activity) {
		callProgramsDialog(activity, true, true);
	}

	/**
	 * Calls ProgramsDialog to open
	 * @param activity Activity which method has called
	 * @param btnInfoVisibility If true shows info button
	 * @param btnInfoWithMenu If true adds popup menu to info button
	 */
	public void callProgramsDialog(FragmentActivity activity,
								   boolean btnInfoVisibility,
								   boolean btnInfoWithMenu) {
		callProgramsDialog(activity, btnInfoVisibility, btnInfoWithMenu, activity.getString(R.string.mah_ads_info_popup_text), Constants.MAH_ADS_GITHUB_LINK);
	}

	/**
	 * Calls ProgramsDialog to open
	 * @param activity Activity which method has called
	 * @param btnInfoVisibility If true shows info button
	 * @param btnInfoWithMenu If true adds popup menu to info button
	 * @param btnInfoMenuItemTitle Title of menu item for info button
	 * @param btnInfoActionURL Url to open when clicking to info button or info menu item
	 */
	public void callProgramsDialog(FragmentActivity activity,
										  boolean btnInfoVisibility,
										  boolean btnInfoWithMenu,
										  String btnInfoMenuItemTitle,
										  @NonNull String btnInfoActionURL) {
		showDlg(activity,
				MAHAdsDlgPrograms.newInstance(mahRequestResult, urlRootOnServer, fontName, btnInfoVisibility, btnInfoWithMenu, btnInfoMenuItemTitle, btnInfoActionURL),
				Constants.TAG_MAH_ADS_DLG_PROGRAMS);
	}

	public static void showDlg(FragmentActivity activity, Fragment frag, String fragTag) {

		if (!activity.isFinishing()) {
			FragmentManager fragmentManager = activity.getSupportFragmentManager();
			Fragment fr = fragmentManager.findFragmentByTag(fragTag);
			if (fr != null && !fr.isHidden()) {
				Log.i(Constants.LOG_TAG_MAH_ADS, "showDlg  dismissed");
                ((DialogFragment) fr).dismissAllowingStateLoss();
			}

			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(frag, fragTag);
			transaction.commitAllowingStateLoss();
		}
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	private boolean isInternalCalled() {
		return internalCalled;
	}

	private void setInternalCalled(boolean internalCalled) {
		this.internalCalled = internalCalled;
	}

	public MAHRequestResult getMahRequestResult() {
		return mahRequestResult;
	}

	public void setMahRequestResult(MAHRequestResult mahRequestResult) {
		this.mahRequestResult = mahRequestResult;
	}
}
