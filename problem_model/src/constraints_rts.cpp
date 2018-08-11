#include <iostream>
#include <algorithm>
#include <exception>

#include "constraints_rts.hpp"

using namespace std;
using namespace ghost;

Leq::Leq( const vector<reference_wrapper< Variable> >& variables,
	  const vector< double >& coeff, double rhs):
    Constraint	( variables ),
    _coeff	( coeff), _rhs(rhs)
{ 
}

double Leq::required_cost() const 
{
  double sum = 0.;

  for( int i = 0 ; i < _coeff.size() ; ++i )
  {
    sum += variables[i].get().get_value() * _coeff[i];
    // cout << variables[i].get_value()<< endl;
  }
  // std::cout<< sum<<endl;
  return std::max( 0., sum - _rhs );
}

Geq::Geq( const vector< reference_wrapper<Variable> >& variables,
	  const vector< double >& coeff, double rhs):
    Constraint	( variables ),
    _coeff	( coeff), _rhs(rhs)
{ 
}

double Geq::required_cost() const 
{
  double sum = 0.;

  for( int i = 0 ; i < _coeff.size() ; ++i )
  {
    sum += variables[i].get().get_value() * _coeff[i];
    // cout << variables[i].get_value()<< endl;
  }
  // std::cout<< sum<<endl;
  return std::max( 0., _rhs - sum );
}

Leq_param::Leq_param( const vector<reference_wrapper< Variable> >& variables,
	  const vector< double >& coeff, int ressources, int pl, int ph, int pr):
    Constraint	( variables ),
    _coeff	( coeff),
    _ressources(ressources), _pl(pl), _ph(ph), _pr(pr)
{ }

double Leq_param::required_cost() const 
{
  double sum = 0.;

  for( int i = 0 ; i < _coeff.size() ; ++i )
  {
    sum += variables[i].get().get_value() * _coeff[i];
  }

  // cout<< "\n" << sum<<endl;

  sum -= (3*_ph +  2*_pr + 2*_pl );
  // cout<< "\n" << sum<<endl;
  return std::max( 0., sum - _ressources );
}
