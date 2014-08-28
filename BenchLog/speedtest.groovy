#!/bin/env groovy

import ProgressLogger;
// http://www.groovyexamples.org/2010/06/03/generate-a-uuid/
import java.util.UUID;


def random = new Random();

def to = 100000;

List contracts = [];
List pcs = [];
List rels = [];

//ProgressLogger pl = new ProgressLogger(to, {println(it)}, 100, 'Test1');
ProgressLogger.measure(
	{println(it)},
	{
		(0..to).each{
			//     pl.next()
			def pcUID = UUID.randomUUID();
			def cUID = UUID.randomUUID();
			//     println random.nextInt(10);
			pcs.add( origId: pcUID );
			contracts.add( origId: cUID );
			rels.add( idPC: pcUID, idContr: cUID );
		}

		pcs.sort{ it.origId }
		contracts.sort{ it.origId }
	},
	'Gen',
	'Generate initial data'
);

//println pcs;
//println contracts;
//println rels;

//List l = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ].toList();
pl = new ProgressLogger(rels, {println(it)}, 100);
rels.each{rel->
	pl.next();
//	def pc = pcs.find{ it.origId == rel.idPC }
	def pc = Collections.binarySearch(pcs, rel.idPC, [ compare: {element, needed-> element.origId.compareTo(needed) } ] as Comparator)
	if (!pc){
		println 'Error! PC not found!'
	}

//	def constract = contracts.find{ it.origId == rel.idContr }
	def contract = Collections.binarySearch(contracts, rel.idContr, [ compare: {element, needed-> element.origId.compareTo(needed) } ] as Comparator)
	if (!contract){
		println 'Error! Contract not found!'
	}
}