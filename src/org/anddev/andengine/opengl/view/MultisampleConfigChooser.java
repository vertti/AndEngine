package org.anddev.andengine.opengl.view;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import org.anddev.andengine.util.Debug;

import android.opengl.GLSurfaceView;

/**
 * TODO Check with GLES1 version for DEPTH_SIZE...
 * (c) Zynga 2011
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 15:31:48 - 04.08.2011
 */
public class MultisampleConfigChooser implements GLSurfaceView.EGLConfigChooser {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MULTISAMPLE_COUNT = 2; // TODO Could be made variable?

	private static final int[] BUFFER = new int[1];

	private static final int EGL_OPENGL_ES2_BIT = 4;

	private static final int[] EGLCONFIG_MULTISAMPLE = {
		EGL10.EGL_RED_SIZE, 5,
		EGL10.EGL_GREEN_SIZE, 6,
		EGL10.EGL_BLUE_SIZE, 5,
		EGL10.EGL_DEPTH_SIZE, 0,
		EGL10.EGL_RENDERABLE_TYPE, MultisampleConfigChooser.EGL_OPENGL_ES2_BIT,
		EGL10.EGL_SAMPLE_BUFFERS, 1,
		EGL10.EGL_SAMPLES, MultisampleConfigChooser.MULTISAMPLE_COUNT,
		EGL10.EGL_NONE
	};

	private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
	private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

	private static final int[] EGLCONFIG_COVERAGEMULTISAMPLE_NVIDIA = new int[]{
		EGL10.EGL_RED_SIZE, 5,
		EGL10.EGL_GREEN_SIZE, 6,
		EGL10.EGL_BLUE_SIZE, 5,
		EGL10.EGL_DEPTH_SIZE, 0,
		EGL10.EGL_RENDERABLE_TYPE, MultisampleConfigChooser.EGL_OPENGL_ES2_BIT,
		MultisampleConfigChooser.EGL_COVERAGE_BUFFERS_NV, 1,
		MultisampleConfigChooser.EGL_COVERAGE_SAMPLES_NV, MultisampleConfigChooser.MULTISAMPLE_COUNT,  // always 5 in practice on tegra 2
		EGL10.EGL_NONE
	};

	private static final int[] EGLCONFIG_FALLBACK = new int[] {
		EGL10.EGL_RED_SIZE, 5,
		EGL10.EGL_GREEN_SIZE, 6,
		EGL10.EGL_BLUE_SIZE, 5,
		EGL10.EGL_DEPTH_SIZE, 0,
		EGL10.EGL_RENDERABLE_TYPE, MultisampleConfigChooser.EGL_OPENGL_ES2_BIT,
		EGL10.EGL_NONE
	};

	// ===========================================================
	// Fields
	// ===========================================================

	private boolean mCoverageMultiSampling;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean isCoverageMultiSampling() {
		return this.mCoverageMultiSampling;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EGLConfig chooseConfig(final EGL10 pEGL, final EGLDisplay pEGLDisplay) {
		MultisampleConfigChooser.BUFFER[0] = 0;

		if(pEGL.eglChooseConfig(pEGLDisplay, MultisampleConfigChooser.EGLCONFIG_MULTISAMPLE, null, 0, MultisampleConfigChooser.BUFFER) == false) {
			throw new IllegalArgumentException("EGLCONFIG_MULTISAMPLE failed!");
		}
		int numConfigs = MultisampleConfigChooser.BUFFER[0];

		if(numConfigs > 0) {
			Debug.d("Using MultiSampling EGLConfig!");
		} else {
			if(pEGL.eglChooseConfig(pEGLDisplay, MultisampleConfigChooser.EGLCONFIG_COVERAGEMULTISAMPLE_NVIDIA, null, 0, MultisampleConfigChooser.BUFFER) == false) {
				throw new IllegalArgumentException("EGLCONFIG_COVERAGEMULTISAMPLE_NVIDIA failed!");
			}
			numConfigs = MultisampleConfigChooser.BUFFER[0];

			if(numConfigs > 0) {
				this.mCoverageMultiSampling = true;
				Debug.d("Using CoverageMultiSampling EGLConfig!");
			} else {
				if(pEGL.eglChooseConfig(pEGLDisplay, MultisampleConfigChooser.EGLCONFIG_FALLBACK, null, 0, MultisampleConfigChooser.BUFFER) == false) {
					throw new IllegalArgumentException("EGLCONFIG_FALLBACK failed!");
				}
				numConfigs = MultisampleConfigChooser.BUFFER[0];

				if(numConfigs > 0) {
					Debug.d("Using fallback EGLConfig!");
				} else {
					throw new IllegalArgumentException("No EGLConfig found!");
				}
			}
		}

		// Get all matching configurations.
		final EGLConfig[] configs = new EGLConfig[numConfigs];
		if(!pEGL.eglChooseConfig(pEGLDisplay, MultisampleConfigChooser.EGLCONFIG_MULTISAMPLE, configs, numConfigs, MultisampleConfigChooser.BUFFER)) {
			throw new IllegalArgumentException("eglChooseConfig failed!");
		}

		return this.findEGLConfig(pEGL, pEGLDisplay, configs);
	}

	private EGLConfig findEGLConfig(final EGL10 pEGL, final EGLDisplay pEGLDisplay, final EGLConfig[] pEGLConfigs) {
		for(int i = 0; i < pEGLConfigs.length; ++i) {
			final EGLConfig config = pEGLConfigs[i];
			if(this.getConfigAttrib(pEGL, pEGLDisplay, config, EGL10.EGL_RED_SIZE, 0) == 5) {
				return config;
			}
		}
		throw new IllegalArgumentException("No EGLConfig found!");
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private int getConfigAttrib(final EGL10 pEGL, final EGLDisplay pEGLDisplay, final EGLConfig pEGLConfig, final int pAttribute, final int pDefaultValue) {
		if(pEGL.eglGetConfigAttrib(pEGLDisplay, pEGLConfig, pAttribute, MultisampleConfigChooser.BUFFER)) {
			return MultisampleConfigChooser.BUFFER[0];
		} else {
			return pDefaultValue;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}