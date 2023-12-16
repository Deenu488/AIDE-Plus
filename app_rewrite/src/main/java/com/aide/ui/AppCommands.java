package com.aide.ui;

import abcd.a9;
import abcd.aa;
import abcd.ab;
import abcd.ac;
import abcd.ad;
import abcd.b9;
import abcd.ba;
import abcd.bb;
import abcd.bc;
import abcd.bd;
import abcd.bg;
import abcd.c9;
import abcd.ca;
import abcd.cb;
import abcd.cc;
import abcd.cd;
import abcd.d9;
import abcd.da;
import abcd.db;
import abcd.dc;
import abcd.dg;
import abcd.e9;
import abcd.ea;
import abcd.eb;
import abcd.ec;
import abcd.f9;
import abcd.fa;
import abcd.fb;
import abcd.fc;
import abcd.g9;
import abcd.ga;
import abcd.gb;
import abcd.gc;
import abcd.h9;
import abcd.ha;
import abcd.hb;
import abcd.hc;
import abcd.i9;
import abcd.ia;
import abcd.ib;
import abcd.ic;
import abcd.j9;
import abcd.ja;
import abcd.jb;
import abcd.jc;
import abcd.k9;
import abcd.ka;
import abcd.kb;
import abcd.kc;
import abcd.l9;
import abcd.la;
import abcd.lb;
import abcd.lc;
import abcd.m9;
import abcd.ma;
import abcd.mb;
import abcd.mc;
import abcd.n9;
import abcd.na;
import abcd.nb;
import abcd.nc;
import abcd.o9;
import abcd.oa;
import abcd.ob;
import abcd.oc;
import abcd.p9;
import abcd.pa;
import abcd.pb;
import abcd.pc;
import abcd.q9;
import abcd.qa;
import abcd.qb;
import abcd.qc;
import abcd.r9;
import abcd.ra;
import abcd.rb;
import abcd.rc;
import abcd.rf;
import abcd.s9;
import abcd.sa;
import abcd.sb;
import abcd.sc;
import abcd.sf;
import abcd.t9;
import abcd.ta;
import abcd.tb;
import abcd.tc;
import abcd.tf;
import abcd.u9;
import abcd.ua;
import abcd.ub;
import abcd.uc;
import abcd.v9;
import abcd.va;
import abcd.vb;
import abcd.vc;
import abcd.w9;
import abcd.wa;
import abcd.wb;
import abcd.wc;
import abcd.x9;
import abcd.xa;
import abcd.xb;
import abcd.xc;
import abcd.y9;
import abcd.ya;
import abcd.yb;
import abcd.yc;
import abcd.z8;
import abcd.z9;
import abcd.za;
import abcd.zb;
import abcd.zc;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import io.github.zeroaicy.aide.utils.FilesRenameMenu;

public class AppCommands{
    private static sf[] DW;
    private static List<bg> EQ;
    private static sf[] FH;
    private static sf[] Hw;
    private static List<bg> J0;
    private static List<bg> J8;

    private static sf[] VH;
    private static sf[] Zo;
    private static HashSet<Class<?>> gn;
    private static sf[] j6;
    private static List<bg> tp;
    private static List<sf> u7;
    private static sf[] v5;
    private static List<bg> we;

    static {
        try{
            j6 = new sf[]{new lb(), new ob(), new kb(), new mb(), new jb(), new pc(), new cb(), new xa(), new dc(), new vc(), new uc(), new tc(), new wc(), new ec(), new wb(), new ub(), new hc(), new i9(), new g9(), new ca(), new bc(), new gb()};
            DW = new sf[]{new s9(), new ib(), new nb(), new qc(), new a9(), new r9(), new oa(), new oc(), new yc(), new nc(), new FilesRenameMenu(), new v9(), new m9(), new q9(), new p9(), new ic(), new z8(), new xb(), new ta(), new ua(), new qa(), new sa(), new pa(), new ra(), new na()};
            FH = new sf[]{new ga(), new ia(), new fa(), new ha(), new f9(), new tb(), new cc(), new e9(), new n9()};
            Hw = new sf[0];
            v5 = new sf[0];
            Zo = new sf[]{new aa(), new kc(), new b9(), new rc(), new j9(), new z9(), new y9(), new cd()};
            VH = new sf[]{new c9(), new h9(), new pb(), new mc(), new lc(), new hb(), new wa(), new va(), new za(), new ab(), new bb(), new sc(), new fc(), new ac(), new gc(), new x9(), new bd(), new vb(), new u9(), new l9(), new sb(), new yb(), new ka(), new d9(), new w9(), new ba(), new ya(), new jc(), new la(), new ja(), new rb(), new ad(), new db(), new eb(), new da(), new ea(), new xc(), new t9(), new o9(), new qb(), new ma(), new fb(), new k9(), new zc()};
            gn = new HashSet<>();
            u7 = new ArrayList<>();
            tp = new ArrayList<>();
            EQ = new ArrayList<>();
            we = new ArrayList<>();
            J0 = new ArrayList<>();
            J8 = new ArrayList<>();
            j6(Zo, EQ);
            j6(VH, EQ);
            j6(j6, EQ);
            j6(DW, tp);
            j6(j6, tp);
            j6(Hw, we);
            j6(j6, we);
            j6(v5, J0);
            j6(j6, J0);
            j6(FH, J8);
            j6(j6, J8);
        }
		catch (Throwable th){
        }
    }

    public AppCommands(){}

    public static rf DW(int i){
		for ( sf sfVar : Hw() ){
			if ( (sfVar instanceof rf) ){
				rf rfVar = (rf)sfVar;
				if ( i == rfVar.gn() ){
					return rfVar;
				}
			}
		}
		return null;
    }

    public static List<tf> FH(){
        List<tf> arrayList = new ArrayList<>();
		for ( sf sfVar : Hw() ){
			if ( sfVar instanceof tf ){
				arrayList.add((tf)sfVar);
			}
		}
		return arrayList;
    }

    public static List<sf> Hw(){
		return u7;
    }

    public static List<bg> VH(){
		return tp;
    }

    public static List<bg> Zo(){
		return we;
    }

    public static List<bg> gn(){
		return J8;
    }

    private static void j6(sf[] sfVarArr, List<bg> list){
        for ( sf sfVar : sfVarArr ){
			if ( sfVar instanceof bg ){
				list.add((bg) sfVar);
			}
			if ( !gn.contains(sfVar.getClass()) ){
				gn.add(sfVar.getClass());
				u7.add(sfVar);
			}
		}
    }

    public static List<bg> tp(){
		return J0;
    }

	static List<dg> dg;
	public static List<dg> dgList(){
		if ( dg == null ){
			dg = new ArrayList<>();
			for ( sf sfVar : Hw() ){
				if ( (sfVar instanceof dg) ){
					dg.add(((dg)sfVar));
				}
			}
		}
		return dg;
	}

    public static dg u7(int id){
		for ( dg dgVar : dgList() ){
			if ( id == dgVar.FH() ){
				return dgVar;
			}
		}
		return null;
    }

    public static List<bg> v5(){
		return EQ;
    }
}
