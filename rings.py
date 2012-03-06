# -*- coding: utf-8 -*-
from ru.leventov.eudfa import Ring, Equations

def ring(length, accepts):
	return Ring.byAccepts(length, accepts)

def solve(left, product):
	return Equations.solveRight(left, product)






