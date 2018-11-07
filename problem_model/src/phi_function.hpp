#pragma once

#include <cmath>
#include <functional>

std::function<double(double)> logistic( int lambda )
{
  return [lambda](double p){ return 1.0 / ( 1 + exp( - lambda * (2*p - 1) ) ); };
}

std::function<double(double)> inverse_logistic( int lambda )
{
  return [lambda](double p)
  {
    double exponential = exp( - lambda * (2*p - 1) );
    return exponential / ( 1 + exponential );
  };
}

std::function<double(double)> logit( int lambda )
{
  return [lambda](double p)
  {
    if( p < 0.005 ) return 0.0;
    if( p > 0.995 ) return 1.0;
    return 0.5 + log( p / ( 1 - p ) ) / (lambda*5) ;
  };
}

std::function<double(double)> flat()
{
  return [](double p){ return 0.5; };
}

std::function<double(double)> neutral()
{
  return [](double p){ return p; };
}
