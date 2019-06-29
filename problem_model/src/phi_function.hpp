#pragma once

#include <cmath>
#include <functional>
#include <algorithm>

std::function<double(double)> logistic( int lambda, double shift )
{
	return [lambda, shift](double p){ return 1.0 / ( 1 + exp( - lambda * (2*p - shift) ) ); };
}

std::function<double(double)> inverse_logistic( int lambda )
{
	return [lambda](double p)
				 {
					 double exponential = exp( - lambda * (2*p - 1) );
					 return exponential / ( 1 + exponential );
				 };
}

std::function<double(double)> log( int lambda, double shift )
{
	return [lambda, shift](double p){ return shift + log(lambda*p)/lambda; };
}

std::function<double(double)> logit( int lambda )
{
	return [lambda](double p)
				 {
					 if( p < 0.005 ) return 0.0;
					 if( p > 0.995 ) return 1.0;
					 return std::max( 0., 1 + log( p / ( 2 - p ) ) / (lambda*2) ) ;
				 };
}

std::function<double(double)> flat()
{
	return [](double p){ return 0.5; };
}

std::function<double(double)> identity()
{
	return [](double p){ return p; };
}

////////////////

// logistic(10, 1.3)
std::function<double(double)> pessimistic()
{
	return [](double p){ return 1.0 / ( 1 + exp( - 10 * (2*p - 1.3) ) ); };
}

// logit(2)
std::function<double(double)> optimistic()
{
	return [](double p)
				 {
					 if( p < 0.005 ) return 0.0;
					 if( p > 0.995 ) return 1.0;
					 return std::max( 0., 1 + ( log( p / ( 2 - p ) ) / 10 ) ) ;
				 };
}

// inverse logistic respecting phi(0)=0 and phi(1)=1
// std::function<double(double)> optimistic()
// {
//   return [](double p)
//   {
//     if( p < 0.005 ) return 0.0;
//     if( p > 0.995 ) return 1.0;
//     return 7*p - 19*pow(p,2) + 13*pow(p,3);
//     // return 0.04027005 + 7.37812*p - 19.3993*pow(p,2) + 12.95399*pow(p,3);
//   };
// }
