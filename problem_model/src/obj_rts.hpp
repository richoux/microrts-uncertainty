#pragma once

#include <string>
#include <vector>

#include "ghost/objective.hpp"
#include "ghost/variable.hpp"


using namespace std;
using namespace ghost;

class MaxDiff : public Objective
{
  vector< double > _coeff;
  vector<vector<int>> _samples;
  
  double required_cost( const vector< Variable >& vecVariables ) const override;

public:
  MaxDiff( const vector< double >& coeff, const vector<vector<int>>& samples );
};