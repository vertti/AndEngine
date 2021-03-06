package org.andengine.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.UpdateHandlerList;
import org.andengine.entity.modifier.EntityModifierList;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.andengine.opengl.util.GLState;
import org.andengine.util.ParameterCallable;
import org.andengine.util.SmartList;
import org.andengine.util.color.Color;
import org.andengine.util.constants.Constants;
import org.andengine.util.transformation.Transformation;


/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 12:00:48 - 08.03.2010
 */
public class Entity implements IEntity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CHILDREN_CAPACITY_DEFAULT = 4;
	private static final int ENTITYMODIFIERS_CAPACITY_DEFAULT = 4;
	private static final int UPDATEHANDLERS_CAPACITY_DEFAULT = 4;

	private static final float[] VERTICES_SCENE_TO_LOCAL_TMP = new float[2];
	private static final float[] VERTICES_LOCAL_TO_SCENE_TMP = new float[2];

	private static final ParameterCallable<IEntity> PARAMETERCALLABLE_DETACHCHILD = new ParameterCallable<IEntity>() {
		@Override
		public void call(final IEntity pEntity) {
			pEntity.setParent(null);
			pEntity.onDetached();
		}
	};

	// ===========================================================
	// Fields
	// ===========================================================

	protected boolean mVisible = true;
	protected boolean mIgnoreUpdate = false;
	protected boolean mChildrenVisible = true;
	protected boolean mChildrenIgnoreUpdate = false;
	protected boolean mChildrenSortPending = false;
	protected boolean mDrawChildrenBehindParent = false;
	
	protected int mZIndex = 0;

	private IEntity mParent;

	protected SmartList<IEntity> mChildren;
	private EntityModifierList mEntityModifiers;
	private UpdateHandlerList mUpdateHandlers;

	protected Color mColor = new Color(1, 1, 1, 1);

	protected float mX;
	protected float mY;

	private final float mInitialX;
	private final float mInitialY;

	protected float mRotation = 0;

	protected float mRotationCenterX = 0;
	protected float mRotationCenterY = 0;

	protected float mScaleX = 1;
	protected float mScaleY = 1;

	protected float mScaleCenterX = 0;
	protected float mScaleCenterY = 0;

	protected float mSkewX = 0;
	protected float mSkewY = 0;

	protected float mSkewCenterX = 0;
	protected float mSkewCenterY = 0;

	private boolean mLocalToParentTransformationDirty = true;
	private boolean mParentToLocalTransformationDirty = true;

	private final Transformation mLocalToParentTransformation = new Transformation();
	private final Transformation mParentToLocalTransformation = new Transformation();

	private final Transformation mLocalToSceneTransformation = new Transformation();
	private final Transformation mSceneToLocalTransformation = new Transformation();

	private Object mUserData;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Entity() {
		this(0, 0);
	}

	public Entity(final float pX, final float pY) {
		this.mInitialX = pX;
		this.mInitialY = pY;

		this.mX = pX;
		this.mY = pY;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected void onUpdateColor() {

	}

	@Override
	public boolean isVisible() {
		return this.mVisible;
	}

	@Override
	public void setVisible(final boolean pVisible) {
		this.mVisible = pVisible;
	}

	@Override
	public boolean isChildrenVisible() {
		return this.mChildrenVisible;
	}

	@Override
	public void setChildrenVisible(final boolean pChildrenVisible) {
		this.mChildrenVisible = pChildrenVisible;
	}

	@Override
	public boolean isIgnoreUpdate() {
		return this.mIgnoreUpdate;
	}

	@Override
	public void setIgnoreUpdate(final boolean pIgnoreUpdate) {
		this.mIgnoreUpdate = pIgnoreUpdate;
	}

	@Override
	public boolean isChildrenIgnoreUpdate() {
		return this.mChildrenIgnoreUpdate;
	}

	@Override
	public void setChildrenIgnoreUpdate(final boolean pChildrenIgnoreUpdate) {
		this.mChildrenIgnoreUpdate = pChildrenIgnoreUpdate;
	}

	@Override
	public boolean hasParent() {
		return this.mParent != null;
	}

	@Override
	public IEntity getParent() {
		return this.mParent;
	}

	@Override
	public void setParent(final IEntity pEntity) {
		this.mParent = pEntity;
	}

	@Override
	public int getZIndex() {
		return this.mZIndex;
	}

	@Override
	public void setZIndex(final int pZIndex) {
		this.mZIndex = pZIndex;
	}

	@Override
	public float getX() {
		return this.mX;
	}

	@Override
	public float getY() {
		return this.mY;
	}

	@Override
	public float getInitialX() {
		return this.mInitialX;
	}

	@Override
	public float getInitialY() {
		return this.mInitialY;
	}

	@Override
	public void setPosition(final IEntity pOtherEntity) {
		this.setPosition(pOtherEntity.getX(), pOtherEntity.getY());
	}

	@Override
	public void setPosition(final float pX, final float pY) {
		this.mX = pX;
		this.mY = pY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setInitialPosition() {
		this.mX = this.mInitialX;
		this.mY = this.mInitialY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public float getRotation() {
		return this.mRotation;
	}

	@Override
	public boolean isRotated() {
		return this.mRotation != 0;
	}

	@Override
	public void setRotation(final float pRotation) {
		this.mRotation = pRotation;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public float getRotationCenterX() {
		return this.mRotationCenterX;
	}

	@Override
	public float getRotationCenterY() {
		return this.mRotationCenterY;
	}

	@Override
	public void setRotationCenterX(final float pRotationCenterX) {
		this.mRotationCenterX = pRotationCenterX;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setRotationCenterY(final float pRotationCenterY) {
		this.mRotationCenterY = pRotationCenterY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setRotationCenter(final float pRotationCenterX, final float pRotationCenterY) {
		this.mRotationCenterX = pRotationCenterX;
		this.mRotationCenterY = pRotationCenterY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public boolean isScaled() {
		return this.mScaleX != 1 || this.mScaleY != 1;
	}

	@Override
	public float getScaleX() {
		return this.mScaleX;
	}

	@Override
	public float getScaleY() {
		return this.mScaleY;
	}

	@Override
	public void setScaleX(final float pScaleX) {
		this.mScaleX = pScaleX;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setScaleY(final float pScaleY) {
		this.mScaleY = pScaleY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setScale(final float pScale) {
		this.mScaleX = pScale;
		this.mScaleY = pScale;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setScale(final float pScaleX, final float pScaleY) {
		this.mScaleX = pScaleX;
		this.mScaleY = pScaleY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public float getScaleCenterX() {
		return this.mScaleCenterX;
	}

	@Override
	public float getScaleCenterY() {
		return this.mScaleCenterY;
	}

	@Override
	public void setScaleCenterX(final float pScaleCenterX) {
		this.mScaleCenterX = pScaleCenterX;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setScaleCenterY(final float pScaleCenterY) {
		this.mScaleCenterY = pScaleCenterY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setScaleCenter(final float pScaleCenterX, final float pScaleCenterY) {
		this.mScaleCenterX = pScaleCenterX;
		this.mScaleCenterY = pScaleCenterY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public boolean isSkewed() {
		return this.mSkewX != 0 || this.mSkewY != 0;
	}

	@Override
	public float getSkewX() {
		return this.mSkewX;
	}

	@Override
	public float getSkewY() {
		return this.mSkewY;
	}

	@Override
	public void setSkewX(final float pSkewX) {
		this.mSkewX = pSkewX;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setSkewY(final float pSkewY) {
		this.mSkewY = pSkewY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setSkew(final float pSkew) {
		this.mSkewX = pSkew;
		this.mSkewY = pSkew;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setSkew(final float pSkewX, final float pSkewY) {
		this.mSkewX = pSkewX;
		this.mSkewY = pSkewY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public float getSkewCenterX() {
		return this.mSkewCenterX;
	}

	@Override
	public float getSkewCenterY() {
		return this.mSkewCenterY;
	}

	@Override
	public void setSkewCenterX(final float pSkewCenterX) {
		this.mSkewCenterX = pSkewCenterX;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setSkewCenterY(final float pSkewCenterY) {
		this.mSkewCenterY = pSkewCenterY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public void setSkewCenter(final float pSkewCenterX, final float pSkewCenterY) {
		this.mSkewCenterX = pSkewCenterX;
		this.mSkewCenterY = pSkewCenterY;
		this.mLocalToParentTransformationDirty = true;
		this.mParentToLocalTransformationDirty = true;
	}

	@Override
	public boolean isRotatedOrScaledOrSkewed() {
		return this.mRotation != 0 || this.mScaleX != 1 || this.mScaleY != 1 || this.mSkewX != 0 || this.mSkewY != 0;
	}

	/**
	 * Do we draw Entity's children on top or behind the Entity. Default is on top (false).
	 * @return false by default
	 */
	public boolean isDrawChildrenBehindParent() {
		return mDrawChildrenBehindParent;
	}
	
	public void setDrawChildrenBehindParent(final boolean pDrawChildrenBehindParent) {
		this.mDrawChildrenBehindParent = pDrawChildrenBehindParent;
	}
	
	@Override
	public float getRed() {
		return this.mColor.getRed();
	}

	@Override
	public float getGreen() {
		return this.mColor.getGreen();
	}

	@Override
	public float getBlue() {
		return this.mColor.getBlue();
	}

	@Override
	public float getAlpha() {
		return this.mColor.getAlpha();
	}

	@Override
	public Color getColor() {
		return this.mColor;
	}

	@Override
	public void setColor(final Color pColor) {
		this.mColor.set(pColor);

		this.onUpdateColor();
	}

	/**
	 * @param pAlpha from <code>0.0f</code> (transparent) to <code>1.0f</code> (opaque)
	 */
	@Override
	public void setAlpha(final float pAlpha) {
		if(this.mColor.setAlphaChecking(pAlpha)) {
			this.onUpdateColor();
		}
	}

	/**
	 * @param pRed from <code>0.0f</code> to <code>1.0f</code>
	 * @param pGreen from <code>0.0f</code> to <code>1.0f</code>
	 * @param pBlue from <code>0.0f</code> to <code>1.0f</code>
	 */
	@Override
	public void setColor(final float pRed, final float pGreen, final float pBlue) {
		if(this.mColor.setChanging(pRed, pGreen, pBlue)) { // TODO Is this check worth it?
			this.onUpdateColor();
		}
	}

	/**
	 * @param pRed from <code>0.0f</code> to <code>1.0f</code>
	 * @param pGreen from <code>0.0f</code> to <code>1.0f</code>
	 * @param pBlue from <code>0.0f</code> to <code>1.0f</code>
	 * @param pAlpha from <code>0.0f</code> (transparent) to <code>1.0f</code> (opaque)
	 */
	@Override
	public void setColor(final float pRed, final float pGreen, final float pBlue, final float pAlpha) {
		if(this.mColor.setChanging(pRed, pGreen, pBlue, pAlpha)) { // TODO Is this check worth it?
			this.onUpdateColor();
		}
	}

	@Override
	public int getChildCount() {
		if(this.mChildren == null) {
			return 0;
		}
		return this.mChildren.size();
	}

	@Override
	public IEntity getChild(final int pIndex) {
		if(this.mChildren == null) {
			return null;
		}
		return this.mChildren.get(pIndex);
	}

	@Override
	public int getChildIndex(final IEntity pEntity) {
		if (this.mChildren == null || pEntity.getParent() != this) {
			return -1;
		}
		return this.mChildren.indexOf(pEntity);
	}

	@Override
	public boolean setChildIndex(final IEntity pEntity, final int pIndex) {
		if (this.mChildren == null || pEntity.getParent() != this) {
			return false;
		}
		try {
			this.mChildren.remove(pEntity);
			this.mChildren.add(pIndex, pEntity);
			return true;
		} catch (final IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public IEntity getFirstChild() {
		if(this.mChildren == null) {
			return null;
		}
		return this.mChildren.get(0);
	}

	@Override
	public IEntity getLastChild() {
		if(this.mChildren == null) {
			return null;
		}
		return this.mChildren.get(this.mChildren.size() - 1);
	}

	@Override
	public boolean detachSelf() {
		final IEntity parent = this.mParent;
		if(parent != null) {
			return parent.detachChild(this);
		} else {
			return false;
		}
	}

	@Override
	public void detachChildren() {
		if(this.mChildren == null) {
			return;
		}
		this.mChildren.clear(Entity.PARAMETERCALLABLE_DETACHCHILD);
	}

	@Override
	public void attachChild(final IEntity pEntity) throws IllegalStateException {
		if(pEntity.hasParent()) {
			throw new IllegalStateException("pEntity already has a parent!");
		}
		if(this.mChildren == null) {
			this.allocateChildren();
		}
		this.mChildren.add(pEntity);
		pEntity.setParent(this);
		pEntity.onAttached();
	}

	@Override
	public boolean attachChild(final IEntity pEntity, final int pIndex) throws IllegalStateException {
		if(pEntity.hasParent()) {
			throw new IllegalStateException("pEntity already has a parent!");
		}
		if (this.mChildren == null) {
			this.allocateChildren();
		}
		try {
			this.mChildren.add(pIndex, pEntity);
			pEntity.setParent(this);
			pEntity.onAttached();
			return true;
		} catch (final IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public IEntity findChild(final IEntityMatcher pEntityMatcher) {
		if(this.mChildren == null) {
			return null;
		}
		return this.mChildren.find(pEntityMatcher);
	}

	@Override
	public boolean swapChildren(final IEntity pEntityA, final IEntity pEntityB) {
		return this.swapChildren(this.getChildIndex(pEntityA), this.getChildIndex(pEntityB));
	}

	@Override
	public boolean swapChildren(final int pIndexA, final int pIndexB) {
		try {
			Collections.swap(this.mChildren, pIndexA, pIndexB);
			return true;
		} catch (final IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public void sortChildren() {
		this.sortChildren(true);
	}

	@Override
	public void sortChildren(final boolean pImmediate) {
		if(this.mChildren == null) {
			return;
		}
		if(pImmediate) {
			ZIndexSorter.getInstance().sort(this.mChildren);
		} else {
			this.mChildrenSortPending = true;
		}
	}

	@Override
	public void sortChildren(final Comparator<IEntity> pEntityComparator) {
		if(this.mChildren == null) {
			return;
		}
		ZIndexSorter.getInstance().sort(this.mChildren, pEntityComparator);
	}

	@Override
	public boolean detachChild(final IEntity pEntity) {
		if(this.mChildren == null) {
			return false;
		}
		return this.mChildren.remove(pEntity, Entity.PARAMETERCALLABLE_DETACHCHILD);
	}

	@Override
	public IEntity detachChild(final IEntityMatcher pEntityMatcher) {
		if(this.mChildren == null) {
			return null;
		}
		return this.mChildren.remove(pEntityMatcher, Entity.PARAMETERCALLABLE_DETACHCHILD);
	}

	@Override
	public boolean detachChildren(final IEntityMatcher pEntityMatcher) {
		if(this.mChildren == null) {
			return false;
		}
		return this.mChildren.removeAll(pEntityMatcher, Entity.PARAMETERCALLABLE_DETACHCHILD);
	}

	@Override
	public void callOnChildren(final IEntityParameterCallable pEntityParameterCallable) {
		if(this.mChildren == null) {
			return;
		}
		this.mChildren.call(pEntityParameterCallable);
	}

	@Override
	public void callOnChildren(final IEntityParameterCallable pEntityParameterCallable, final IEntityMatcher pEntityMatcher) {
		if(this.mChildren == null) {
			return;
		}
		this.mChildren.call(pEntityMatcher, pEntityParameterCallable);
	}

	@Override
	public void registerUpdateHandler(final IUpdateHandler pUpdateHandler) {
		if(this.mUpdateHandlers == null) {
			this.allocateUpdateHandlers();
		}
		this.mUpdateHandlers.add(pUpdateHandler);
	}

	@Override
	public boolean unregisterUpdateHandler(final IUpdateHandler pUpdateHandler) {
		if(this.mUpdateHandlers == null) {
			return false;
		}
		return this.mUpdateHandlers.remove(pUpdateHandler);
	}

	@Override
	public boolean unregisterUpdateHandlers(final IUpdateHandlerMatcher pUpdateHandlerMatcher) {
		if(this.mUpdateHandlers == null) {
			return false;
		}
		return this.mUpdateHandlers.removeAll(pUpdateHandlerMatcher);
	}

	@Override
	public void clearUpdateHandlers() {
		if(this.mUpdateHandlers == null) {
			return;
		}
		this.mUpdateHandlers.clear();
	}

	@Override
	public void registerEntityModifier(final IEntityModifier pEntityModifier) {
		if(this.mEntityModifiers == null) {
			this.allocateEntityModifiers();
		}
		this.mEntityModifiers.add(pEntityModifier);
	}

	@Override
	public boolean unregisterEntityModifier(final IEntityModifier pEntityModifier) {
		if(this.mEntityModifiers == null) {
			return false;
		}
		return this.mEntityModifiers.remove(pEntityModifier);
	}

	@Override
	public boolean unregisterEntityModifiers(final IEntityModifierMatcher pEntityModifierMatcher) {
		if(this.mEntityModifiers == null) {
			return false;
		}
		return this.mEntityModifiers.removeAll(pEntityModifierMatcher);
	}

	@Override
	public void clearEntityModifiers() {
		if(this.mEntityModifiers == null) {
			return;
		}
		this.mEntityModifiers.clear();
	}

	@Override
	public float[] getSceneCenterCoordinates() {
		return this.convertLocalToSceneCoordinates(0, 0);
	}

	public Transformation getLocalToParentTransformation() {
		final Transformation localToParentTransformation = this.mLocalToParentTransformation;
		if(this.mLocalToParentTransformationDirty) {
			localToParentTransformation.setToIdentity();

			/* Scale. */
			final float scaleX = this.mScaleX;
			final float scaleY = this.mScaleY;
			if(scaleX != 1 || scaleY != 1) {
				final float scaleCenterX = this.mScaleCenterX;
				final float scaleCenterY = this.mScaleCenterY;

				/* TODO Check if it is worth to check for scaleCenterX == 0 && scaleCenterY == 0 as the two postTranslate can be saved.
				 * The same obviously applies for all similar occurrences of this pattern in this class. */

				localToParentTransformation.postTranslate(-scaleCenterX, -scaleCenterY);
				localToParentTransformation.postScale(scaleX, scaleY);
				localToParentTransformation.postTranslate(scaleCenterX, scaleCenterY);
			}

			/* Skew. */
			final float skewX = this.mSkewX;
			final float skewY = this.mSkewY;
			if(skewX != 0 || skewY != 0) {
				final float skewCenterX = this.mSkewCenterX;
				final float skewCenterY = this.mSkewCenterY;

				localToParentTransformation.postTranslate(-skewCenterX, -skewCenterY);
				localToParentTransformation.postSkew(skewX, skewY);
				localToParentTransformation.postTranslate(skewCenterX, skewCenterY);
			}

			/* Rotation. */
			final float rotation = this.mRotation;
			if(rotation != 0) {
				final float rotationCenterX = this.mRotationCenterX;
				final float rotationCenterY = this.mRotationCenterY;

				localToParentTransformation.postTranslate(-rotationCenterX, -rotationCenterY);
				localToParentTransformation.postRotate(rotation);
				localToParentTransformation.postTranslate(rotationCenterX, rotationCenterY);
			}

			/* Translation. */
			localToParentTransformation.postTranslate(this.mX, this.mY);

			this.mLocalToParentTransformationDirty = false;
		}
		return localToParentTransformation;
	}

	public Transformation getParentToLocalTransformation() {
		final Transformation parentToLocalTransformation = this.mParentToLocalTransformation;
		if(this.mParentToLocalTransformationDirty) {
			parentToLocalTransformation.setToIdentity();

			/* Translation. */
			parentToLocalTransformation.postTranslate(-this.mX, -this.mY);

			/* Rotation. */
			final float rotation = this.mRotation;
			if(rotation != 0) {
				final float rotationCenterX = this.mRotationCenterX;
				final float rotationCenterY = this.mRotationCenterY;

				parentToLocalTransformation.postTranslate(-rotationCenterX, -rotationCenterY);
				parentToLocalTransformation.postRotate(-rotation);
				parentToLocalTransformation.postTranslate(rotationCenterX, rotationCenterY);
			}

			/* Skew. */
			final float skewX = this.mSkewX;
			final float skewY = this.mSkewY;
			if(skewX != 0 || skewY != 0) {
				final float skewCenterX = this.mSkewCenterX;
				final float skewCenterY = this.mSkewCenterY;

				parentToLocalTransformation.postTranslate(-skewCenterX, -skewCenterY);
				parentToLocalTransformation.postSkew(-skewX, -skewY);
				parentToLocalTransformation.postTranslate(skewCenterX, skewCenterY);
			}

			/* Scale. */
			final float scaleX = this.mScaleX;
			final float scaleY = this.mScaleY;
			if(scaleX != 1 || scaleY != 1) {
				final float scaleCenterX = this.mScaleCenterX;
				final float scaleCenterY = this.mScaleCenterY;

				parentToLocalTransformation.postTranslate(-scaleCenterX, -scaleCenterY);
				parentToLocalTransformation.postScale(1 / scaleX, 1 / scaleY); // TODO Division could be replaced by a multiplication of 'scale(X/Y)Inverse'...
				parentToLocalTransformation.postTranslate(scaleCenterX, scaleCenterY);
			}

			this.mParentToLocalTransformationDirty = false;
		}
		return parentToLocalTransformation;
	}

	@Override
	public Transformation getLocalToSceneTransformation() {
		// TODO Cache if parent(recursive) not dirty.
		final Transformation localToSceneTransformation = this.mLocalToSceneTransformation;
		localToSceneTransformation.setTo(this.getLocalToParentTransformation());

		final IEntity parent = this.mParent;
		if(parent != null) {
			localToSceneTransformation.postConcat(parent.getLocalToSceneTransformation());
		}

		return localToSceneTransformation;
	}

	@Override
	public Transformation getSceneToLocalTransformation() {
		// TODO Cache if parent(recursive) not dirty.
		final Transformation sceneToLocalTransformation = this.mSceneToLocalTransformation;
		sceneToLocalTransformation.setTo(this.getParentToLocalTransformation());

		final IEntity parent = this.mParent;
		if(parent != null) {
			sceneToLocalTransformation.preConcat(parent.getSceneToLocalTransformation());
		}

		return sceneToLocalTransformation;
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertLocalToSceneCoordinates(float, float)
	 */
	@Override
	public float[] convertLocalToSceneCoordinates(final float pX, final float pY) {
		return this.convertLocalToSceneCoordinates(pX, pY, Entity.VERTICES_LOCAL_TO_SCENE_TMP);
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertLocalToSceneCoordinates(float, float, float[])
	 */
	@Override
	public float[] convertLocalToSceneCoordinates(final float pX, final float pY, final float[] pReuse) {
		final Transformation localToSceneTransformation = this.getLocalToSceneTransformation();

		pReuse[Constants.VERTEX_INDEX_X] = pX;
		pReuse[Constants.VERTEX_INDEX_Y] = pY;

		localToSceneTransformation.transform(pReuse);

		return pReuse;
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertLocalToSceneCoordinates(float[])
	 */
	@Override
	public float[] convertLocalToSceneCoordinates(final float[] pCoordinates) {
		return this.convertSceneToLocalCoordinates(pCoordinates, Entity.VERTICES_LOCAL_TO_SCENE_TMP);
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertLocalToSceneCoordinates(float[], float[])
	 */
	@Override
	public float[] convertLocalToSceneCoordinates(final float[] pCoordinates, final float[] pReuse) {
		final Transformation localToSceneTransformation = this.getLocalToSceneTransformation();

		pReuse[Constants.VERTEX_INDEX_X] = pCoordinates[Constants.VERTEX_INDEX_X];
		pReuse[Constants.VERTEX_INDEX_Y] = pCoordinates[Constants.VERTEX_INDEX_Y];

		localToSceneTransformation.transform(pReuse);

		return pReuse;
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertSceneToLocalCoordinates(float, float)
	 */
	@Override
	public float[] convertSceneToLocalCoordinates(final float pX, final float pY) {
		return this.convertSceneToLocalCoordinates(pX, pY, Entity.VERTICES_SCENE_TO_LOCAL_TMP);
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertSceneToLocalCoordinates(float, float, float[])
	 */
	@Override
	public float[] convertSceneToLocalCoordinates(final float pX, final float pY, final float[] pReuse) {
		pReuse[Constants.VERTEX_INDEX_X] = pX;
		pReuse[Constants.VERTEX_INDEX_Y] = pY;

		this.getSceneToLocalTransformation().transform(pReuse);

		return pReuse;
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertSceneToLocalCoordinates(float[])
	 */
	@Override
	public float[] convertSceneToLocalCoordinates(final float[] pCoordinates) {
		return this.convertSceneToLocalCoordinates(pCoordinates, Entity.VERTICES_SCENE_TO_LOCAL_TMP);
	}

	/* (non-Javadoc)
	 * @see org.andengine.entity.IEntity#convertSceneToLocalCoordinates(float[], float[])
	 */
	@Override
	public float[] convertSceneToLocalCoordinates(final float[] pCoordinates, final float[] pReuse) {
		pReuse[Constants.VERTEX_INDEX_X] = pCoordinates[Constants.VERTEX_INDEX_X];
		pReuse[Constants.VERTEX_INDEX_Y] = pCoordinates[Constants.VERTEX_INDEX_Y];

		this.getSceneToLocalTransformation().transform(pReuse);

		return pReuse;
	}

	@Override
	public void onAttached() {

	}

	@Override
	public void onDetached() {

	}

	@Override
	public Object getUserData() {
		return this.mUserData;
	}

	@Override
	public void setUserData(final Object pUserData) {
		this.mUserData = pUserData;
	}

	@Override
	public final void onDraw(final GLState pGLState, final Camera pCamera) {
		if(this.mVisible) {
			this.onManagedDraw(pGLState, pCamera);
		}
	}

	@Override
	public final void onUpdate(final float pSecondsElapsed) {
		if(!this.mIgnoreUpdate) {
			this.onManagedUpdate(pSecondsElapsed);
		}
	}

	@Override
	public void reset() {
		this.mVisible = true;
		this.mIgnoreUpdate = false;
		this.mChildrenVisible = true;
		this.mChildrenIgnoreUpdate = false;

		this.mX = this.mInitialX;
		this.mY = this.mInitialY;
		this.mRotation = 0;
		this.mScaleX = 1;
		this.mScaleY = 1;
		this.mSkewX = 0;
		this.mSkewY = 0;

		this.mColor.reset();

		if(this.mEntityModifiers != null) {
			this.mEntityModifiers.reset();
		}

		if(this.mChildren != null) {
			final ArrayList<IEntity> entities = this.mChildren;
			for(int i = entities.size() - 1; i >= 0; i--) {
				entities.get(i).reset();
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		this.toString(stringBuilder);
		return stringBuilder.toString();
	}

	@Override
	public void toString(final StringBuilder pStringBuilder) {
		pStringBuilder.append(this.getClass().getSimpleName());

		if(this.mChildren != null && this.mChildren.size() > 0) {
			pStringBuilder.append(" [");
			final ArrayList<IEntity> entities = this.mChildren;
			for(int i = 0; i < entities.size(); i++) {
				entities.get(i).toString(pStringBuilder);
				if(i < entities.size() - 1) {
					pStringBuilder.append(", ");
				}
			}
			pStringBuilder.append("]");
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @param pGLState the currently active {@link GLState} i.e. to apply transformations to.
	 * @param pCamera the currently active {@link Camera} i.e. to be used for culling.
	 */
	protected void preDraw(final GLState pGLState, final Camera pCamera) {

	}

	/**
	 * @param pGLState the currently active {@link GLState} i.e. to apply transformations to.
	 * @param pCamera the currently active {@link Camera} i.e. to be used for culling.
	 */
	protected void draw(final GLState pGLState, final Camera pCamera) {

	}

	/**
	 * @param pGLState the currently active {@link GLState} i.e. to apply transformations to.
	 * @param pCamera the currently active {@link Camera} i.e. to be used for culling.
	 */
	protected void postDraw(final GLState pGLState, final Camera pCamera) {

	}

	private void allocateEntityModifiers() {
		this.mEntityModifiers = new EntityModifierList(this, Entity.ENTITYMODIFIERS_CAPACITY_DEFAULT);
	}

	private void allocateChildren() {
		this.mChildren = new SmartList<IEntity>(Entity.CHILDREN_CAPACITY_DEFAULT);
	}

	private void allocateUpdateHandlers() {
		this.mUpdateHandlers = new UpdateHandlerList(Entity.UPDATEHANDLERS_CAPACITY_DEFAULT);
	}

	protected void onApplyTransformations(final GLState pGLState) {
		/* Translation. */
		this.applyTranslation(pGLState);

		/* Rotation. */
		this.applyRotation(pGLState);

		/* Skew. */
		this.applySkew(pGLState);

		/* Scale. */
		this.applyScale(pGLState);
	}

	protected void applyTranslation(final GLState pGLState) {
		pGLState.translateModelViewGLMatrixf(this.mX, this.mY, 0);
	}

	protected void applyRotation(final GLState pGLState) {
		final float rotation = this.mRotation;

		if(rotation != 0) {
			final float rotationCenterX = this.mRotationCenterX;
			final float rotationCenterY = this.mRotationCenterY;

			pGLState.translateModelViewGLMatrixf(rotationCenterX, rotationCenterY, 0);
			pGLState.rotateModelViewGLMatrixf(rotation, 0, 0, 1);
			pGLState.translateModelViewGLMatrixf(-rotationCenterX, -rotationCenterY, 0);

			/* TODO There is a special, but very likely case when mRotationCenter and mScaleCenter are the same.
			 * In that case the last glTranslatef of the rotation and the first glTranslatef of the scale is superfluous.
			 * The problem is that applyRotation and applyScale would need to be "merged" in order to efficiently check for that condition.  */
		}
	}

	protected void applySkew(final GLState pGLState) {
		final float skewX = this.mSkewX;
		final float skewY = this.mSkewY;

		if(skewX != 0 || skewY != 0) {
			final float skewCenterX = this.mSkewCenterX;
			final float skewCenterY = this.mSkewCenterY;

			pGLState.translateModelViewGLMatrixf(skewCenterX, skewCenterY, 0);
			pGLState.skewModelViewGLMatrixf(skewX, skewY);
			pGLState.translateModelViewGLMatrixf(-skewCenterX, -skewCenterY, 0);
		}
	}

	protected void applyScale(final GLState pGLState) {
		final float scaleX = this.mScaleX;
		final float scaleY = this.mScaleY;

		if(scaleX != 1 || scaleY != 1) {
			final float scaleCenterX = this.mScaleCenterX;
			final float scaleCenterY = this.mScaleCenterY;

			pGLState.translateModelViewGLMatrixf(scaleCenterX, scaleCenterY, 0);
			pGLState.scaleModelViewGLMatrixf(scaleX, scaleY, 1);
			pGLState.translateModelViewGLMatrixf(-scaleCenterX, -scaleCenterY, 0);
		}
	}

	protected void onManagedDraw(final GLState pGLState, final Camera pCamera) {
		pGLState.pushModelViewGLMatrix();
		{
			this.onApplyTransformations(pGLState);

			if (mDrawChildrenBehindParent) this.onDrawChildren(pGLState, pCamera);
			
			this.preDraw(pGLState, pCamera);
			this.draw(pGLState, pCamera);
			this.postDraw(pGLState, pCamera);

			if (!mDrawChildrenBehindParent) this.onDrawChildren(pGLState, pCamera);
		}
		pGLState.popModelViewGLMatrix();
	}

	protected void onDrawChildren(final GLState pGLState, final Camera pCamera) {
		if(this.mChildren != null && this.mChildrenVisible) {
			if(this.mChildrenSortPending) {
				ZIndexSorter.getInstance().sort(this.mChildren);
			}
			this.onManagedDrawChildren(pGLState, pCamera);
		}
	}

	public void onManagedDrawChildren(final GLState pGLState, final Camera pCamera) {
		final ArrayList<IEntity> children = this.mChildren;
		final int childCount = children.size();
		for(int i = 0; i < childCount; i++) {
			children.get(i).onDraw(pGLState, pCamera);
		}
	}

	protected void onManagedUpdate(final float pSecondsElapsed) {
		if(this.mEntityModifiers != null) {
			this.mEntityModifiers.onUpdate(pSecondsElapsed);
		}
		if(this.mUpdateHandlers != null) {
			this.mUpdateHandlers.onUpdate(pSecondsElapsed);
		}

		if(this.mChildren != null && !this.mChildrenIgnoreUpdate) {
			final ArrayList<IEntity> entities = this.mChildren;
			final int entityCount = entities.size();
			for(int i = 0; i < entityCount; i++) {
				entities.get(i).onUpdate(pSecondsElapsed);
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
