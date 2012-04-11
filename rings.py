# -*- coding: utf-8 -*-
from ru.leventov.eudfa import Ring, Equations

def ring(length, accepts):
	return Ring.byAccepts(length, accepts)

def solve(left, product):
	return Equations.solve(left, product)

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


