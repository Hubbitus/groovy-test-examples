
import common.runners.SQLExecute
import common.sql.Connection
import common.sql.ConnectionType
import common.sql.DbSqlConnections
import common.sql.SqlCode
import common.sql.SqlCodeMerge

config{
	results{
		// Set null of false to disable write
		metaInfoFile = 'results/meta.xml'
		// Debug data to write full resultsDataDiff. It also allow then continue to process analyzing its data. For example for various formating
		metaInfoDataFile = 'results/meta.full.xml';
//		metaInfoDataFile = null;
		/**
		 * File to write result diffs
		 */
		metaInfoDiffFile = 'results/meta.diff.xml';
		/**
		 * File to write result diffs in xls format
		 */
		metaInfoDiffFileXls = 'results/meta.diff.xls';
		/**
		 * File to write result diffs
		 */
		metaInfoProfillingFile = 'results/meta.profiling.xml';
	}
	checks {
		// What checks do
//		active = [ telezone.supply, telezone.consume, actzone.supply, actzone.consume, eitpzone.supply, contrzone.supply, plans.byzone.supply, plans.byzone.consume, plans.full ]
		// All without plans:
		active = [ telezone.supply, telezone.consume, actzone.supply, actzone.consume, eitpzone.supply, contrzone.supply ]
//		active = [ telezone.supply ]
//		active = [ telezone.consume ]
//		active = [ actzone.supply ]
//		active = [ actzone.consume ]
//		active = [ eitpzone.supply ]
//		active = [ contrzone.supply ]
//		active = [ plans.byzone.supply ]
//		active = [ plans.byzone.consume ]
//		active = [ plans.full ]

		// Name of connection in pool which threat as main (MRG) to try compare with each other
		mainNameToCompareWithOthers = ['МРГ', 'МРГ-ПРОД', 'МРГ-ПРОД-']

		// ARC_EXPS_(D|H), ext_system_id = 10 (I_ZONE|S_ZONE)
		telezone{
			common{
				// Base SQL query which will be constructed for different fields set, tables, zones and params
				baseQuery = new SqlCodeMerge(
					'''SELECT -fields-
FROM
	ARC_EXPS_D d
	JOIN CHANNEL ch ON (d.CHANNEL_ID = ch.CHANNEL_ID)
	JOIN EQUIP e ON (e.EQUIP_ID = ch.EQUIP_ID)
	JOIN TUUG t ON (t.UUG_ID = e.UUG_ID)
	JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)
	JOIN BALANCABLE_ZONE bz ON (bz.ZONE_ID = z.ZONE_ID)
WHERE
	1=1
	AND d.EXT_SYSTEM_ID = 10
	AND d.CORR_TIME BETWEEN :dateStart AND :dateEnd
	AND bz.ZONE_NAME LIKE :zone
ORDER BY
	bz.ZONE_NAME, d.CORR_TIME'''
					,[
						dateStart: new Date().parse('yyyy-MM-dd', '2013-12-25').toTimestamp()
						,dateEnd: new Date().parse('yyyy-MM-dd', '2013-12-25').toTimestamp()
						// See mainObjectAdditionWhere for data separation in main
						,zone: 'Газпром межрегионгаз %'
					]
				)
				// List of fields which and how select to compare data. Must have two columns: id, md5
				fieldsHash = new SqlCodeMerge(
					(ConnectionType.COMMON): '''
	d.id, bz.ZONE_NAME, d.CORR_TIME
	,RAWTOHEX( DBMS_OBFUSCATION_TOOLKIT.md5(input_string =>
		bz.ZONE_NAME || TO_CHAR(d.CORR_TIME, 'YYYY-MM-DD HH24:MI:SS') || COALESCE(REPLACE(ROUND(d.EXPS_NU, 10), ',', '.'), 'null') || d.EXT_SYSTEM_ID || COALESCE(d.ORIG_SYSTEM_ID, -1) /* || ch.CHANNEL_NAME*/ || ch.OWNER_CODE_SYSTEM || ch.ORIG_ID || COALESCE(ch.DVIS, 0) /*|| e.EQUIP_NAME*/ || e.OWNER_CODE_SYSTEM || e.ORIG_ID || COALESCE(e.DVIS, 0) /*|| t.UUG_NAME*/ || t.OWNER_CODE_SYSTEM || t.ORIG_ID || COALESCE(t.DVIS, 0)
	) ) as md5'''
					// Float to string conversion - http://stackoverflow.com/questions/3715675/how-to-convert-float-to-varchar-in-sql-server
					,(ConnectionType.MSSQL): '''
	d.id, bz.ZONE_NAME, d.CORR_TIME
	,CONVERT(VARCHAR(32), HashBytes(
		'MD5'
		,bz.ZONE_NAME + CONVERT(VARCHAR(20), d.CORR_TIME, 120) +
		COALESCE(
			CASE
				WHEN 0 = ROUND(d.exps_nu, 10) THEN '0'
				WHEN LEN(REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(CONVERT(VARCHAR, CONVERT(DECIMAL(38, 10), d.exps_nu)), '0', ' '))), ' ', '0'),'.',' ')),' ','.')) < LEN(REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(STR(d.exps_nu, 38, 10), '0', ' '))), ' ', '0'),'.',' ')),' ','.'))
					THEN REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(CONVERT(VARCHAR, CONVERT(DECIMAL(38, 10), d.exps_nu)), '0', ' '))), ' ', '0'),'.',' ')),' ','.')
				ELSE REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(STR(d.exps_nu, 38, 10), '0', ' '))), ' ', '0'),'.',' ')),' ','.')
			END
			,'null'
		)
		+ LTRIM(STR(d.EXT_SYSTEM_ID)) + LTRIM(STR(COALESCE(d.ORIG_SYSTEM_ID, -1))) /*+ ch.CHANNEL_NAME*/ + ch.OWNER_CODE_SYSTEM + ch.ORIG_ID + LTRIM(STR(COALESCE(ch.DVIS, 0))) /*+ e.EQUIP_NAME*/ + e.OWNER_CODE_SYSTEM + e.ORIG_ID + LTRIM(STR(COALESCE(e.DVIS, 0))) /*+ t.UUG_NAME*/ + t.OWNER_CODE_SYSTEM + t.ORIG_ID + LTRIM(STR(COALESCE(t.DVIS, 0)))
	), 2) as md5'''
				)
				// List fields of data to select when it does not matched. For troubleshooting
				fieldsData = new SqlCodeMerge(
					(ConnectionType.COMMON): '''bz.ZONE_NAME, d.CORR_TIME, d.EXPS_NU, REPLACE(ROUND(d.EXPS_NU, 10), ',', '.') STR_EXPS_NU, d.EXT_SYSTEM_ID, d.ORIG_SYSTEM_ID, ch.CHANNEL_ID/*, ch.CHANNEL_NAME*/, ch.OWNER_CODE_SYSTEM as CH_O_C_S, ch.ORIG_ID as CH_ORIG_ID, ch.DVIS as CH_DVIS, e.EQUIP_ID/*, e.EQUIP_NAME*/, e.OWNER_CODE_SYSTEM as E_O_C_S, e.ORIG_ID as E_ORIG_ID, e.DVIS as E_DVIS, t.UUG_ID/*, t.UUG_NAME*/, t.OWNER_CODE_SYSTEM as UUG_O_C_S, t.ORIG_ID as UUG_ORIG_ID, t.DVIS as UUG_DVIS'''
					// For exps_nu conversion see: http://connectsql.blogspot.ru/2011/04/normal-0-microsoftinternetexplorer4.html
					,(ConnectionType.MSSQL): '''bz.ZONE_NAME, d.CORR_TIME, d.EXPS_NU
, CASE
	WHEN 0 = ROUND(d.exps_nu, 10) THEN '0'
	WHEN LEN(REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(CONVERT(VARCHAR, CONVERT(DECIMAL(38, 10), d.exps_nu)), '0', ' '))), ' ', '0'),'.',' ')),' ','.')) < LEN(REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(STR(d.exps_nu, 38, 10), '0', ' '))), ' ', '0'),'.',' ')),' ','.'))
		THEN REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(CONVERT(VARCHAR, CONVERT(DECIMAL(38, 10), d.exps_nu)), '0', ' '))), ' ', '0'),'.',' ')),' ','.')
	ELSE REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(STR(d.exps_nu, 38, 10), '0', ' '))), ' ', '0'),'.',' ')),' ','.')
END STR_EXPS_NU, d.EXT_SYSTEM_ID, d.ORIG_SYSTEM_ID, ch.CHANNEL_ID/*, ch.CHANNEL_NAME*/, ch.OWNER_CODE_SYSTEM as CH_O_C_S, ch.ORIG_ID as CH_ORIG_ID, ch.DVIS as CH_DVIS, e.EQUIP_ID/*, e.EQUIP_NAME*/, e.OWNER_CODE_SYSTEM as E_O_C_S, e.ORIG_ID as E_ORIG_ID, e.DVIS as E_DVIS, t.UUG_ID/*, t.UUG_NAME*/, t.OWNER_CODE_SYSTEM as UUG_O_C_S, t.ORIG_ID as UUG_ORIG_ID, t.DVIS as UUG_DVIS'''
				)
				// Closure which take CommonRun parameter represent data and return SqlCodeMerge by it to add as
				// additional where condition in baseQuery. Primarily to filter MRG data by zone, owner_code_system and so on
				mainObjectAdditionWhere = {SQLExecute other->
					if (!other.res){
						other.error += "; Other data empty for (${other.name}). Did it queried?"
						return new SqlCodeMerge('1=2');
					}
					new SqlCodeMerge("bz.ZONE_NAME IN (${other.res.collect{ "'${it.ZONE_NAME}'" }.unique().join(',') })")
				}
				idField = 'd.id'
				compare{
					// Rules to compare rows in dataset and provide detailed columns diff
					rows{
						// Set of field names by what try to compare not matched sets of data. In UPPER case!
						keyFields = [ 'CH_O_C_S', 'CH_ORIG_ID', 'ZONE_NAME', 'CORR_TIME' ]
						// Fields to exclude from comparison (from fieldsHash + fieldsData field sets). In UPPER case!
						exludeFields = [ 'ID', /* 'MD5',*/ 'CHANNEL_ID', 'EQUIP_ID', 'UUG_ID' ]
					}
				}
			}
			supply {
				name = 'TeleZone ARC_EXPS_D, ext_system_id = 10, I_ZONE (supply). Зона КИО - поставка'
				// @TODO extend ConfigSlurper and ConfigObject to provide parent automatically
				// From which object get missed config data
				parent = checks.telezone.common
			}
			consume {
				name = 'TeleZone ARC_EXPS_D, ext_system_id = 10, S_ZONE (consume). Зона КИО - потребление'
				baseQuery = checks.telezone.common.baseQuery
					.merge('JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)', 'JOIN S_ZONE z ON (z.SUUG_ID = t.UUG_ID)')
				parent = checks.telezone.common
			}
		}
		// ARC_EXPS_(D|H), ext_system_id = 15 (I_ZONE|S_ZONE)
		eitpzone{
			supply{
				name = 'EITPZone ARC_EXPS_D, ext_system_id = 15, I_ZONE (supply). Зона ЕИТП - поставка'
				baseQuery = checks.telezone.common.baseQuery
					.merge('AND d.EXT_SYSTEM_ID = 10', 'AND d.EXT_SYSTEM_ID = 15')
				parent = checks.telezone.common
			}
			// No consume in EITP
		}
		// ADD_ARC_EXPS_(D|H), ext_system_id = 70 (I_ZONE|S_ZONE)
		actzone{
			supply{
				name = 'ActZone ADD_ARC_EXPS_D, ext_system_id = 70, I_ZONE (supply). Зона Актирования - поставка'
				baseQuery = checks.telezone.common.baseQuery
					.merge('ARC_EXPS_D d', 'ADD_ARC_EXPS_D d')
						.merge('AND d.EXT_SYSTEM_ID = 10', 'AND d.EXT_SYSTEM_ID = 70')
				parent = checks.telezone.common
			}
			consume {
				name = 'ActZone ADD_ARC_EXPS_D, ext_system_id = 70, S_ZONE (consume). Зона Актирования - потребление'
				parent = checks.telezone.common
				baseQuery = checks.telezone.common.baseQuery
					.merge('JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)', 'JOIN S_ZONE z ON (z.SUUG_ID = t.UUG_ID)')
						.merge('ARC_EXPS_D d', new SqlCode('ADD_ARC_EXPS_D d'))
							.merge('AND d.EXT_SYSTEM_ID = 10', 'AND d.EXT_SYSTEM_ID = 70')
			}
		}
		contrzone{
			common{
				baseQuery = new SqlCodeMerge(
					'''SELECT -fields-
FROM
	CONTR_ARC_EXPS_D d
	JOIN CONTRACT_CONNECTION cc ON (cc.CC_ID = d.CC_ID)
	JOIN POINT_CONNECT pc ON (pc.PC_ID = cc.PC_ID)
	JOIN CHANNEL ch ON (ch.CHANNEL_ID = pc.CHANNEL_ID)
	JOIN EQUIP e ON (e.EQUIP_ID = ch.EQUIP_ID)
	JOIN TUUG t ON (t.UUG_ID = e.UUG_ID)
	JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)
	JOIN BALANCABLE_ZONE bz ON (bz.ZONE_ID = z.ZONE_ID)
	--
	JOIN CONTRACT c ON (c.contract_id = cc.contract_id)
	LEFT JOIN PURCHASE_CONTRACT pcntr ON (pcntr.pcontr_id = cc.pcontr_id)
	LEFT JOIN PRIORITY_MARK pr ON (pr.pr_mark_id = cc.pr_mark_id)
WHERE
	1=1
	AND d.ext_system_id = 70
	AND d.CORR_TIME BETWEEN :dateStart AND :dateEnd
	AND bz.ZONE_NAME LIKE :zone'''
					,(Map)telezone.common.baseQuery.params
				)
				fieldsHash = new SqlCodeMerge(
					(ConnectionType.COMMON): '''
	d.id, bz.ZONE_NAME, d.corr_time
	,RAWTOHEX( DBMS_OBFUSCATION_TOOLKIT.md5(input_string =>
		bz.ZONE_NAME || ':' || TO_CHAR(d.CORR_TIME, 'YYYY-MM-DD HH24:MI:SS') || ':' || COALESCE(REPLACE(ROUND(d.EXPS_NU, 10), ',', '.'), 'null') || ':' ||
			d.EXT_SYSTEM_ID || ':' ||
			cc.owner_code_system || ':' || cc.orig_id || ':' || COALESCE(TO_CHAR(cc.DATE_FROM, 'YYYY-MM-DD HH24:MI:SS'), 'null') || ':' || COALESCE(TO_CHAR(cc.DATE_TO, 'YYYY-MM-DD HH24:MI:SS'), 'null') || ':' || COALESCE(cc.DVIS, 0) || ':' ||
			pc.owner_code_system || ':' || pc.orig_id || ':' || COALESCE(pc.DVIS, 0) || ':' ||
			COALESCE(cc.cons_type_id, -1) || ':' ||
			c.owner_code_system || ':' || c.orig_id || ':' || COALESCE(c.DVIS, 0) || ':' ||
			COALESCE(pcntr.owner_code_system, 'null') || ':' || COALESCE(pcntr.orig_id, 'null') || ':' || COALESCE(pcntr.DVIS, 0) || ':' ||
			COALESCE(pr.owner_code_system, 'null') || ':' || COALESCE(pr.orig_id, 'null') || ':' || COALESCE(pr.DVIS, 0)
	) ) as md5'''
					,(ConnectionType.MSSQL): '''
	d.id, bz.ZONE_NAME, d.corr_time
	,CONVERT(VARCHAR(32), HashBytes(
		'MD5'
		,bz.ZONE_NAME + ':' + CONVERT(VARCHAR(20), d.CORR_TIME, 120) + ':' + COALESCE(CASE WHEN 0 = ROUND(d.exps_nu, 10) THEN '0' ELSE REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(CONVERT(VARCHAR, CONVERT(DECIMAL(38, 10), d.exps_nu)), '0', ' '))), ' ', '0'),'.',' ')),' ','.') END, 'null') + ':' +
			LTRIM(STR(d.EXT_SYSTEM_ID)) + ':' +
			cc.owner_code_system + ':' + cc.orig_id + ':' + COALESCE(CONVERT(VARCHAR(20), cc.date_from, 120), 'null') + ':' + COALESCE(CONVERT(VARCHAR(20), cc.date_to, 120), 'null') + ':' + LTRIM(STR(COALESCE(cc.DVIS, 0))) + ':' +
			pc.owner_code_system + ':' + pc.orig_id + ':' + LTRIM(STR(COALESCE(pc.DVIS, 0))) + ':' +
			LTRIM(STR(COALESCE(cc.cons_type_id, -1))) + ':' +
			c.owner_code_system + ':' + c.orig_id + ':' + LTRIM(STR(COALESCE(c.DVIS, 0))) + ':' +
			COALESCE(pcntr.owner_code_system, 'null') + ':' + COALESCE(pcntr.orig_id, 'null') + ':' + LTRIM(STR(COALESCE(pcntr.DVIS, 0))) + ':' +
			COALESCE(pr.owner_code_system, 'null') + ':' + COALESCE(pr.orig_id, 'null') + ':' + LTRIM(STR(COALESCE(pr.DVIS, 0)))
	), 2) as md5'''
				)
				fieldsData = new SqlCodeMerge(
					(ConnectionType.COMMON): '''d.exps_nu
	,REPLACE(ROUND(d.EXPS_NU, 10), ',', '.') as str_exps_nu
	,cc.owner_code_system as cc_O_C_S, cc.orig_id as cc_orig_id, cc.date_from CC_DATE_FROM, cc.date_to CC_DATE_TO, cc.dvis as cc_dvis
	,pc.owner_code_system as pc_o_c_s, pc.orig_id as pc_orig_id, pc.dvis as pc_dvis
	,c.owner_code_system as c_O_C_S, c.orig_id as c_orig_id, c.dvis as c_dvis
	,cc.cons_type_id -- imus.common
	,pcntr.owner_code_system as pcntr_o_c_s, pcntr.orig_id as pcntr_orig_id, pcntr.dvis as pcntr_dvis
	,pr.owner_code_system as pr_o_c_s, pr.orig_id as pr_orig_id, pr.dvis as pr_dvis'''
					,(ConnectionType.MSSQL): '''d.exps_nu
	,CASE WHEN 0 = ROUND(d.exps_nu, 10) THEN '0' ELSE REPLACE(RTRIM(REPLACE(REPLACE(RTRIM(LTRIM(REPLACE(CONVERT(VARCHAR, CONVERT(DECIMAL(38, 10), d.exps_nu)), '0', ' '))), ' ', '0'),'.',' ')),' ','.') END as str_exps_nu
	,cc.owner_code_system as cc_O_C_S, cc.orig_id as cc_orig_id, cc.date_from CC_DATE_FROM, cc.date_to CC_DATE_TO, cc.dvis as cc_dvis
	,pc.owner_code_system as pc_o_c_s, pc.orig_id as pc_orig_id, pc.dvis as pc_dvis
	,c.owner_code_system as c_O_C_S, c.orig_id as c_orig_id, c.dvis as c_dvis
	,cc.cons_type_id -- imus.common
	,pcntr.owner_code_system as pcntr_o_c_s, pcntr.orig_id as pcntr_orig_id, pcntr.dvis as pcntr_dvis
	,pr.owner_code_system as pr_o_c_s, pr.orig_id as pr_orig_id, pr.dvis as pr_dvis'''
				)
				idField = 'd.id'
				compare{
					// Rules to compare rows in dataset and provide detailed columns diff
					rows{
						// Set of field names by what try to compare not matched sets of data. In UPPER case!
						keyFields = [ 'CC_O_C_S', 'CC_ORIG_ID', 'ZONE_NAME', 'CORR_TIME' ]
						// Fields to exclude from comparison (from fieldsHash + fieldsData field sets). In UPPER case!
						exludeFields = [ 'ID', /* 'MD5',*/ ]
					}
				}
				mainObjectAdditionWhere = checks.telezone.common.mainObjectAdditionWhere
			}
			supply{
				parent = checks.contrzone.common
				name = 'ContrZone CONTR_ARC_EXPS_D, ext_system_id = 70, I_ZONE (Supply). Зона распределения - поставка'
			}
// Have not many sense
//			consume{
//				parent = checks.contrzone.common
//				name = 'ContrZone ARC_EXPS_D, ext_system_id = 70, S_ZONE (Consume). Зона распределения - потребление'
//				baseQuery = checks.contrzone.common.baseQuery
//					.merge('JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)', 'JOIN S_ZONE z ON (z.SUUG_ID = t.UUG_ID)')
//						.merge('JOIN CHANNEL ch ON (ch.CHANNEL_ID = pc.CHANNEL_ID)', 'JOIN CHANNEL ch ON (ch.pc_id = pc.pc_id)')
//			}
		}
		plans{
			common{
				baseQuery = new SqlCodeMerge(
					'''SELECT -fields-
FROM
	CC_DAILY_PLAN pln
	JOIN CONTRACT_CONNECTION cc ON (cc.CC_ID = pln.CC_ID)
	JOIN POINT_CONNECT pc ON (pc.PC_ID = cc.PC_ID)
	JOIN CHANNEL ch ON (ch.CHANNEL_ID = pc.CHANNEL_ID)
	JOIN EQUIP e ON (e.EQUIP_ID = ch.EQUIP_ID)
	JOIN TUUG t ON (t.UUG_ID = e.UUG_ID)
	JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)
	JOIN BALANCABLE_ZONE bz ON (bz.ZONE_ID = z.ZONE_ID)
	--
	JOIN CONTRACT c ON (c.contract_id = cc.contract_id)
	LEFT JOIN PURCHASE_CONTRACT pcntr ON (pcntr.pcontr_id = cc.pcontr_id)
	LEFT JOIN PRIORITY_MARK pr ON (pr.pr_mark_id = cc.pr_mark_id)
WHERE
	1=1
	AND pln.date_for BETWEEN :dateStart AND :dateEnd
	AND bz.ZONE_NAME LIKE :zone'''
					,(Map)telezone.common.baseQuery.params
				)
				fieldsHash = new SqlCodeMerge(
					(ConnectionType.COMMON): '''
	pln.pid as id, bz.ZONE_NAME, pln.date_for
	,RAWTOHEX( DBMS_OBFUSCATION_TOOLKIT.md5(input_string =>
		bz.ZONE_NAME || ':' || TO_CHAR(pln.date_for, 'YYYY-MM-DD HH24:MI:SS') || ':' || pln.day_plan || ':' || pln.disp_limit || ':' ||
			cc.owner_code_system || ':' || cc.orig_id || ':' || COALESCE(TO_CHAR(cc.DATE_FROM, 'YYYY-MM-DD HH24:MI:SS'), 'null') || ':' || COALESCE(TO_CHAR(cc.DATE_TO, 'YYYY-MM-DD HH24:MI:SS'), 'null') || ':' || COALESCE(cc.DVIS, 0) || ':' ||
			pc.owner_code_system || ':' || pc.orig_id || ':' || COALESCE(pc.DVIS, 0) || ':' ||
			COALESCE(cc.cons_type_id, -1) || ':' ||
			c.owner_code_system || ':' || c.orig_id || ':' || COALESCE(c.DVIS, 0) || ':' ||
			COALESCE(pcntr.owner_code_system, 'null') || ':' || COALESCE(pcntr.orig_id, 'null') || ':' || COALESCE(pcntr.DVIS, 0) || ':' ||
			COALESCE(pr.owner_code_system, 'null') || ':' || COALESCE(pr.orig_id, 'null') || ':' || COALESCE(pr.DVIS, 0)
	) ) as md5'''
					,(ConnectionType.MSSQL): '''
	pln.pid as id, bz.ZONE_NAME, pln.date_for
	,CONVERT(VARCHAR(32), HashBytes(
		'MD5'
		,bz.ZONE_NAME + ':' + CONVERT(VARCHAR(20), pln.date_for, 120) + ':' + LTRIM(STR(pln.day_plan)) + ':' + LTRIM(STR(pln.disp_limit)) + ':' +
			cc.owner_code_system + ':' + cc.orig_id + ':' + COALESCE(CONVERT(VARCHAR(20), cc.date_from, 120), 'null') + ':' + COALESCE(CONVERT(VARCHAR(20), cc.date_to, 120), 'null') + ':' + LTRIM(STR(COALESCE(cc.DVIS, 0))) + ':' +
			pc.owner_code_system + ':' + pc.orig_id + ':' + LTRIM(STR(COALESCE(pc.DVIS, 0))) + ':' +
			LTRIM(STR(COALESCE(cc.cons_type_id, -1))) + ':' +
			c.owner_code_system + ':' + c.orig_id + ':' + LTRIM(STR(COALESCE(c.DVIS, 0))) + ':' +
			COALESCE(pcntr.owner_code_system, 'null') + ':' + COALESCE(pcntr.orig_id, 'null') + ':' + LTRIM(STR(COALESCE(pcntr.DVIS, 0))) + ':' +
			COALESCE(pr.owner_code_system, 'null') + ':' + COALESCE(pr.orig_id, 'null') + ':' + LTRIM(STR(COALESCE(pr.DVIS, 0)))
	), 2) as md5'''
				)
				fieldsData = new SqlCodeMerge(
					(ConnectionType.COMMON): '''pln.day_plan, pln.disp_limit
	,cc.owner_code_system as CC_O_C_S, cc.orig_id as CC_ORIG_ID, cc.date_from CC_DATE_FROM, cc.date_to CC_DATE_TO, cc.dvis as CC_DVIS
	,pc.owner_code_system as PC_O_C_S, pc.orig_id as PC_ORIG_ID, pc.dvis as PC_DVIS
	,c.owner_code_system as C_O_C_S, c.orig_id as C_ORIG_ID, c.dvis as C_DVIS
	,cc.cons_type_id -- imus.common
	,pcntr.owner_code_system as pcntr_o_c_s, pcntr.orig_id as pcntr_orig_id, pcntr.dvis as pcntr_dvis
	,pr.owner_code_system as pr_o_c_s, pr.orig_id as pr_orig_id, pr.dvis as pr_dvis'''
					,(ConnectionType.MSSQL): '''pln.day_plan, pln.disp_limit
	,cc.owner_code_system as CC_O_C_S, cc.orig_id as cc_orig_id, cc.date_from CC_DATE_FROM, cc.date_to CC_DATE_TO, cc.dvis as CC_DVIS
	,pc.owner_code_system as pc_o_c_s, pc.orig_id as pc_orig_id, pc.dvis as pc_dvis
	,c.owner_code_system as c_O_C_S, c.orig_id as c_orig_id, c.dvis as c_dvis
	,cc.cons_type_id -- imus.common
	,pcntr.owner_code_system as pcntr_o_c_s, pcntr.orig_id as pcntr_orig_id, pcntr.dvis as pcntr_dvis
	,pr.owner_code_system as pr_o_c_s, pr.orig_id as pr_orig_id, pr.dvis as pr_dvis'''
				)
				compare{
					// Rules to compare rows in dataset and provide detailed columns diff
					rows{
						// Set of field names by what try to compare not matched sets of data. In UPPER case!
						keyFields = [ 'CC_O_C_S', 'CC_ORIG_ID', 'ZONE_NAME', 'DATE_FOR' ]
						// Fields to exclude from comparison (from fieldsHash + fieldsData field sets). In UPPER case!
						exludeFields = [ 'ID', /* 'MD5',*/ ]
					}
				}
				mainObjectAdditionWhere = checks.telezone.common.mainObjectAdditionWhere
				idField = 'pln.pid'
			}
			byzone{
				supply{
					parent = checks.plans.common
					name = 'Plans CC_DAILY_PLAN, I_ZONE (supply). Планы - поставка'
				}
				consume{
					parent = checks.plans.common
					name = 'Plans CC_DAILY_PLAN, S_ZONE (consume). Планы - потребление'
					baseQuery = checks.plans.common.baseQuery
						.merge('JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)', 'JOIN S_ZONE z ON (z.SUUG_ID = t.UUG_ID)')
							.merge('JOIN CHANNEL ch ON (ch.CHANNEL_ID = pc.CHANNEL_ID)', 'JOIN CHANNEL ch ON (ch.pc_id = pc.pc_id)')
				}
			}
			// By system without zone
			full{
				name = 'Plans CC_DAILY_PLAN, Full by owner_code_system. Планы - полные (без зоны, по системе)'
				parent = checks.plans.common
				baseQuery = plans.common.baseQuery
					.merge('AND bz.ZONE_NAME LIKE :zone', '''AND cc.owner_code_system LIKE '%' ''')
						.merge('JOIN CHANNEL ch ON (ch.CHANNEL_ID = pc.CHANNEL_ID)', '')
							.merge('JOIN EQUIP e ON (e.EQUIP_ID = ch.EQUIP_ID)', '')
								.merge('JOIN TUUG t ON (t.UUG_ID = e.UUG_ID)', '')
									.merge('JOIN I_ZONE z ON (z.IUUG_ID = t.UUG_ID)', '')
										.merge('JOIN BALANCABLE_ZONE bz ON (bz.ZONE_ID = z.ZONE_ID)', '')
				fieldsHash = plans.common.fieldsHash
					.merge('bz.ZONE_NAME, ', 'cc.owner_code_system as CC_O_C_S, ')
						.merge('bz.ZONE_NAME', 'cc.owner_code_system') // hash count
//							.merge("bz.ZONE_NAME + ':' + ", '')
				mainObjectAdditionWhere = {SQLExecute other->
					if (!other.res){
						other.error += "; Other data empty for (${other.name}). Did it queried?"
						return new SqlCodeMerge('1=2');
					}
					new SqlCodeMerge("cc.owner_code_system IN (${other.res.collect{ "'${it.CC_O_C_S}'" }.unique().join(',') })")
				}
				compare{
					// Rules to compare rows in dataset and provide detailed columns diff
					rows{
						// Set of field names by what try to compare not matched sets of data. In UPPER case!
						keyFields = [ 'CC_O_C_S', 'CC_ORIG_ID', 'DATE_FOR' ]
						// Fields to exclude from comparison (from fieldsHash + fieldsData field sets). In UPPER case!
						exludeFields = [ 'ID', /* 'MD5',*/ ]
					}
				}
			}
		}
	}
}