#include <numeric>
#include <iostream>

#include "obj_rts.hpp"

using namespace std;


double phi(const double& p){ 
  return 1 - (1-p)*(1-p)*(1-p)*(1-p)*(1-p); 
  // return p;
  // return p*p;
}

MaxDiff::MaxDiff( const vector< double >& coeff, const vector<vector<int>>& samples )
  : Objective( "Max Stochastic diff" ), _coeff(coeff), _samples(samples)   
{
    // for(int i=0; i < _coeff.size(); ++i){
    //     cout << endl;
    // }
 }


double MaxDiff::required_cost( const vector< Variable >& vecVariables ) const 
{
    vector<double> sols;

    int N = (int)_samples.size();
    
    for( int i = 0 ; i < N ; ++i )
    {
      // cout << _coeff[0] << " * " << vecVariables[0].get_value() << " + " << _coeff[1] << " * " << vecVariables[1].get_value() << " - " << _samples[i][0] << endl;
      // cout << _coeff[2] << " * " << vecVariables[2].get_value() << " + " << _coeff[3] << " * " << vecVariables[3].get_value() << " - " << _samples[i][1] << endl;
      // cout << _coeff[4] << " * " << vecVariables[4].get_value() <<  " - " << _samples[i][2] << endl;
      // cout << _coeff[5] << " * " << vecVariables[5].get_value() <<  " - " << _samples[i][3] << endl;
      // double tmp = std::min(0., _coeff[0] * vecVariables[0].get_value() + _coeff[1] * vecVariables[1].get_value() - _samples[i][0]) + 
      //     std::min(0., _coeff[2] * vecVariables[2].get_value() + _coeff[3] * vecVariables[3].get_value() - _samples[i][1]) + 
      //     std::min(0., _coeff[4] * vecVariables[4].get_value() - _samples[i][2]) + 
      //     std::min(0., _coeff[5] * vecVariables[5].get_value() - _samples[i][3]);
      // sample : W/H/R/L

      // double tmp = std::min(1., _coeff[0] * vecVariables[0].get_value() + _coeff[1] * vecVariables[1].get_value() + _coeff[2] * vecVariables[2].get_value()- _samples[i][1]) + //vs heavy
      //       std::min(1., _coeff[3] * vecVariables[3].get_value() + _coeff[4] * vecVariables[4].get_value() + _coeff[5] * vecVariables[5].get_value()- _samples[i][3]) + //vs light
      //       std::min(1., _coeff[6] * vecVariables[6].get_value() + _coeff[7] * vecVariables[7].get_value() + _coeff[8] * vecVariables[8].get_value()- _samples[i][2]) //vs ranged
      //       ;

      double tmp = _coeff[0] * vecVariables[0].get_value() + _coeff[1] * vecVariables[1].get_value() + _coeff[2] * vecVariables[2].get_value() - _samples[i][1] + //vs heavy
	_coeff[3] * vecVariables[3].get_value() + _coeff[4] * vecVariables[4].get_value() + _coeff[5] * vecVariables[5].get_value()- _samples[i][3] + //vs light
	_coeff[6] * vecVariables[6].get_value() + _coeff[7] * vecVariables[7].get_value() + _coeff[8] * vecVariables[8].get_value()- _samples[i][2]; //vs ranged
      
      sols.push_back(tmp);
      // cout << " ==> "<< tmp<<endl;
    }
    
    std::sort(sols.begin(), sols.end());

    double Ceu = sols[0];



    for( int i = 1 ; i < sols.size() ; ++i )
      Ceu += (sols[i] - sols[i-1]) * phi( static_cast<double>( N - i ) / N );

    // cout << "===   CEU = "<< Ceu<<" ===\n\n";
    

    // if(Ceu <= 10.81){
        // for(int i = 0; i < vecVariables.size(); ++i){
        //     cout << vecVariables[i].get_value()<< endl;
        // }
    // }


    return -Ceu;

  // return std::accumulate( begin( vecVariables ),
  // 			  end( vecVariables ),
  // 			  0,
  // 			  []( auto& v ){
  // 			    Unit* u = dynamic_cast<Unit*>( v.get() );
  // 			    return v.is_assigned() ? v.get_value() * v.get_dps() : 0;
  // 			  } ); 
}
