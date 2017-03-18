package net.qsoft.docview;

import java.util.Date;

public class FileDesc {
	private String _Name;
	private String _Type;
	private Date _Date;
	private Long _DLId;
	private String _DnName;
	
	/**
	 * @param _Name
	 * @param _Type
	 * @param _Date
	 * @param _DLId
	 */
	public FileDesc(String _Name, String _Type, Date _Date, Long _DLId) {
		super();
		this._Name = _Name;
		this._Type = _Type;
		this._Date = _Date;
		this._DLId = _DLId;
	}

	public FileDesc() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the _Name
	 */
	public final String get_Name() {
		return _Name;
	}

	/**
	 * @param _Name the _Name to set
	 */
	public final void set_Name(String _Name) {
		this._Name = _Name;
	}

	/**
	 * @return the _Type
	 */
	public final String get_Type() {
		return _Type;
	}

	/**
	 * @param _Type the _Type to set
	 */
	public final void set_Type(String _Type) {
		this._Type = _Type;
	}

	/**
	 * @return the _Date
	 */
	public final Date get_Date() {
		return _Date;
	}

	/**
	 * @param _Date the _Date to set
	 */
	public final void set_Date(Date _Date) {
		this._Date = _Date;
	}

	/**
	 * @return the _DLId
	 */
	public final Long get_DLId() {
		return _DLId;
	}

	/**
	 * @param _DLId the _DLId to set
	 */
	public final void set_DLId(Long _DLId) {
		this._DLId = _DLId;
	}

	public String get_DnName() {
		return _DnName;
	}

	public void set_DnName(String _DnName) {
		this._DnName = _DnName;
	}

}
