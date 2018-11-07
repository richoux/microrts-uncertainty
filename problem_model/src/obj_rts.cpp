#include <numeric>
#include <iostream>

#include "obj_rts.hpp"

using namespace std;

// double phi(const double p){ 
//   return 1 - (1-p)*(1-p)*(1-p)*(1-p)*(1-p); 
//   // return p;
//   // return p*p;
// }

MaxDiff::MaxDiff( const vector< double >& coeff,
		  const vector<vector<int>>& samples,
		  std::function<double(double)> phi )
  : Objective( "Max Stochastic diff" ), _coeff(coeff), _samples(samples), phi(phi)   
{ }


double MaxDiff::required_cost( const vector< Variable >& vecVariables ) const 
{
    vector<double> sols;

    int N = (int)_samples.size();
    
    for( int i = 0 ; i < N ; ++i )
    {
      double tmp = std::min(1., _coeff[0] * vecVariables[0].get_value() + _coeff[1] * vecVariables[1].get_value() + _coeff[2] * vecVariables[2].get_value()- _samples[i][1]) + //vs heavy
	std::min(1., _coeff[3] * vecVariables[3].get_value() + _coeff[4] * vecVariables[4].get_value() + _coeff[5] * vecVariables[5].get_value()- _samples[i][3]) + //vs light
	std::min(1., _coeff[6] * vecVariables[6].get_value() + _coeff[7] * vecVariables[7].get_value() + _coeff[8] * vecVariables[8].get_value()- _samples[i][2]); //vs ranged
      
      // double tmp = - _samples[i][1] //vs heavy
      // 	- _samples[i][2] //vs ranged
      // 	- _samples[i][3]; //vs light

      // for( int j = 0 ; j < 9 ; ++j )
      // 	tmp += _coeff[j] * vecVariables[j].get_value();

      // tmp = std::min( 1., tmp );
      
      sols.push_back(tmp);
    }
    
    std::sort( sols.begin(), sols.end() );

    double RDU = sols[0];

    for( int i = 1 ; i < sols.size() ; ++i )
      RDU += (sols[i] - sols[i-1]) * phi( static_cast<double>( N - i ) / N );

    return -RDU;
}
