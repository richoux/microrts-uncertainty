#pragma once

#include <vector>

#include "ghost/constraint.hpp"
#include "ghost/variable.hpp"


using namespace std;
using namespace ghost;


class Leq : public Constraint
{
  vector< double >	_coeff;
  double _rhs;

  double required_cost() const override;

public:
  Leq( const vector<reference_wrapper<Variable>>& variables,
	  const vector< double >& coeff, double rhs);
};

class Geq : public Constraint
{
  vector< double >	_coeff;
  double _rhs;

  double required_cost() const override;

public:
  Geq( const vector< reference_wrapper<Variable> >& variables,
	  const vector< double >& coeff, double rhs);
};

class Leq_param : public Constraint
{
  vector< double >	_coeff;
  int _ressources;
  int _pl;
  int _ph;
  int _pr;

  double required_cost() const override;

public:
  Leq_param(const vector< reference_wrapper<Variable> >& variables,
	  const vector< double >& coeff, int ressources, int pl, int ph, int pr);
};

