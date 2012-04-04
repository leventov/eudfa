# -*- coding: utf-8 -*-
from ru.leventov.eudfa import Ring, Equations

def ring(length, accepts):
	return Ring.byAccepts(length, accepts)

def solve(left, product, l=None, sorted=False):
	if l: a = Equations.solveRight(left, product, l)
	else: a = Equations.solveRight(left, product)
	if sorted:	
		al = [(len(r.accepts()), r) for r in a]
		al.sort()
		a = [r for (l, r) in al]	
	for r in a:
		print r
	print len(a)

def psolve(left, product, l=None, sorted=False):
	if l: a = Equations.principleSolveRight(left, product, l)
	else: a = Equations.principleSolveRight(left, product)
	if sorted:	
		al = [(len(r.accepts()), r) for r in a]
		al.sort()
		a = [r for (l, r) in al]
	for r in a:
		print r
	print len(a)


def vsolve(left, product):
	pl = product.length()
	a = Equations.principleSolveRight(left, product, pl)
	if a:
		return max(a, lambda r: len(r.getAccepts()))
	return 0




def gen_rings(length, accept_count):
	lim = 1 << length
	rings = []
	for i in range(lim):
		accepts = []
		for k in range(length):
		    if i & (1 << k):
		    	accepts.append(k)
		if len(accepts) == accept_count:
			rings.append(ring(length, accepts))
	return rings

def gen_all(length):
	res = []
	for i in range(1, length):
		res += gen_rings(length, i)
	return res


