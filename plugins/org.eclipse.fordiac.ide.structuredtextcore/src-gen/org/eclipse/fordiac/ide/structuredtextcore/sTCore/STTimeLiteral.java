/**
 * generated by Xtext 2.25.0
 */
package org.eclipse.fordiac.ide.structuredtextcore.sTCore;

import org.eclipse.fordiac.ide.model.data.DataType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ST Time Literal</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.sTCore.STTimeLiteral#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.sTCore.STTimeLiteral#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fordiac.ide.structuredtextcore.sTCore.STCorePackage#getSTTimeLiteral()
 * @model
 * @generated
 */
public interface STTimeLiteral extends STExpression
{
  /**
   * Returns the value of the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the value of the '<em>Type</em>' containment reference.
   * @see #setType(DataType)
   * @see org.eclipse.fordiac.ide.structuredtextcore.sTCore.STCorePackage#getSTTimeLiteral_Type()
   * @model containment="true"
   * @generated
   */
  DataType getType();

  /**
   * Sets the value of the '{@link org.eclipse.fordiac.ide.structuredtextcore.sTCore.STTimeLiteral#getType <em>Type</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Type</em>' containment reference.
   * @see #getType()
   * @generated
   */
  void setType(DataType value);

  /**
   * Returns the value of the '<em><b>Value</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the value of the '<em>Value</em>' attribute.
   * @see #setValue(String)
   * @see org.eclipse.fordiac.ide.structuredtextcore.sTCore.STCorePackage#getSTTimeLiteral_Value()
   * @model
   * @generated
   */
  String getValue();

  /**
   * Sets the value of the '{@link org.eclipse.fordiac.ide.structuredtextcore.sTCore.STTimeLiteral#getValue <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Value</em>' attribute.
   * @see #getValue()
   * @generated
   */
  void setValue(String value);

} // STTimeLiteral
