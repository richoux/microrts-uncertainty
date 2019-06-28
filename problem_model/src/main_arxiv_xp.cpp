/*
 * THIS FILE IS NOT USED, NEITHER FOR THE BOT
 * NOR FOR THE ARXIV/CEC PAPER (ALTHOUGH ITS NAME)
 *
 * So why I keep it?
 *
 */


#include <unistd.h>
#include <ios>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <memory>
#include <random>
#include <algorithm>
#include <vector>
#include <numeric>
//#include <chrono>

#include "ghost/variable.hpp"
#include "ghost/solver.hpp"

#include "constraints_rts.hpp"
#include "obj_rts.hpp"
#include "phi_function.hpp"

using namespace std;


// From https://www.fluentcpp.com/2017/04/21/how-to-split-a-string-in-c/X
vector<string> split( string text )
{
	istringstream iss(text);
	vector<string> results(istream_iterator<string>{iss}, istream_iterator<string>());
	return results;
}

int main(int argc, char *argv[])
{
	if( argc != 4 && argc != 5 )
	{
		cout << "Usage: " << argv[0] << " distribution_file #samples #runs [lambda]\n";
		return EXIT_FAILURE;
	}

	// std::ifstream file( argv[1] );
	int nb_samples = std::stoi( argv[2] );
	int nb_runs = std::stoi( argv[3] );

	// int LAMBDA;
	// if( argc == 5 )
	//   LAMBDA = std::stoi( argv[4] );
	// else
	//   LAMBDA = 5;

	int success_identity = 0;
	// int success_logistic = 0;
	// int success_logit = 0;
	// int success_inverse_logistic = 0;

	random_device			rd;
	mt19937			rng ( rd() );

	// vector<double> worker_vec;
	// vector<double> ranged_vec;
	// vector<double> light_vec;
	// vector<double> heavy_vec;

	for( int loop = 0 ; loop < nb_runs ; ++loop )
	{
		std::ifstream file( argv[1] );
		uniform_int_distribution<int> random_time( 0, 1200 );

		int block = random_time( rng );
		int time = block * 10;

		//cout << "time:" << time << "\n";

		string dumb_line, time_line, worker_line, ranged_line, light_line, heavy_line;

		for( int i = 0 ; i < block * 5 ; ++i )
			std::getline(file, dumb_line);

		std::getline(file, time_line);
		std::getline(file, worker_line);
		std::getline(file, ranged_line);
		std::getline(file, light_line);
		std::getline(file, heavy_line);

		auto worker_data = split( worker_line );
		auto ranged_data = split( ranged_line );
		auto light_data = split( light_line );
		auto heavy_data = split( heavy_line );

		int size_worker = (int)worker_data.size() - 1;
		int size_ranged = (int)ranged_data.size() - 1;
		int size_light= (int)light_data.size() - 1;
		int size_heavy = (int)heavy_data.size() - 1;

		vector<double> worker_vec( size_worker, 0.0 );
		vector<double> ranged_vec( size_ranged, 0.0 );
		vector<double> light_vec( size_light, 0.0 );
		vector<double> heavy_vec( size_heavy, 0.0 );

		for( unsigned long i = 0 ; i < size_worker ; ++i )
			worker_vec[i] = std::stod( worker_data[i+1] );

		for( unsigned long i = 0 ; i < size_ranged ; ++i )
			ranged_vec[i] = std::stod( ranged_data[i+1] );

		for( unsigned long i = 0 ; i < size_light ; ++i )
			light_vec[i] = std::stod( light_data[i+1] );

		for( unsigned long i = 0 ; i < size_heavy ; ++i )
			heavy_vec[i] = std::stod( heavy_data[i+1] );

		std::discrete_distribution<int> worker_distrib( worker_vec.begin(), worker_vec.end() );
		std::discrete_distribution<int> ranged_distrib( worker_vec.begin(), worker_vec.end() );
		std::discrete_distribution<int> light_distrib( worker_vec.begin(), worker_vec.end() );
		std::discrete_distribution<int> heavy_distrib( worker_vec.begin(), worker_vec.end() );

		int enemy_number_workers = worker_distrib( rng );
		int enemy_number_ranged = ranged_distrib( rng );
		int enemy_number_light = light_distrib( rng );
		int enemy_number_heavy = heavy_distrib( rng );

		uniform_int_distribution<int>	random_worker( 0, enemy_number_workers );
		uniform_int_distribution<int>	random_ranged( 0, enemy_number_ranged );
		uniform_int_distribution<int>	random_light( 0, enemy_number_light );
		uniform_int_distribution<int>	random_heavy( 0, enemy_number_heavy );

		std::vector< std::vector< int > > samples;

		/*
		 * Samples indexes:
		 * 0 for worker
		 * 1 for heavy
		 * 2 for ranged
		 * 3 for light
		 */

		for( int i = 0 ; i < nb_samples ; ++i )
		{
			std::vector<int> tmp { random_worker( rng ),
														 random_heavy( rng ),
														 random_ranged( rng ),
														 random_light( rng ) };
			samples.push_back( tmp );
		}

		uniform_int_distribution<int>	my_random_worker( 0, enemy_number_workers + enemy_number_workers/3 );
		uniform_int_distribution<int>	my_random_ranged( 0, enemy_number_ranged + enemy_number_ranged/3 );
		uniform_int_distribution<int>	my_random_light( 0, enemy_number_light + enemy_number_light/3 );
		uniform_int_distribution<int>	my_random_heavy( 0, enemy_number_heavy + enemy_number_heavy/3 );

		int my_number_workers = my_random_worker( rng );
		int my_number_ranged = my_random_ranged( rng );
		int my_number_light = my_random_light( rng );
		int my_number_heavy = my_random_heavy( rng );

		// cout << time << "\n"
		//      << enemy_number_workers << "\n"
		//      << enemy_number_ranged << "\n"
		//      << enemy_number_light << "\n"
		//      << enemy_number_heavy << "\n"
		//      << my_number_workers << "\n"
		//      << my_number_ranged << "\n"
		//      << my_number_light << "\n"
		//      << my_number_heavy << "\n";

		// No matter the resource, we just want to dedice about ONE unit training
		int resources = 3;

		// I kept ph, pr, pl to keep the code similar to the original main.cpp
		int ph = my_number_heavy;
		int pr = my_number_ranged;
		int pl = my_number_light;

		vector<Variable> variables;

		variables.push_back(Variable("Light_heavy", "Lh", 0, 10+pl));
		variables.push_back(Variable("Heavy_heavy", "Hh", 0, 10+ph));
		variables.push_back(Variable("Ranged_heavy", "Rh", 0, 10+pr));

		variables.push_back(Variable("Light_light", "Ll", 0, 10+pl));
		variables.push_back(Variable("Heavy_light", "Hl", 0, 10+ph));
		variables.push_back(Variable("Ranged_light", "Rl", 0, 10+pr));

		variables.push_back(Variable("Light_ranged", "Lr", 0, 10+pl));
		variables.push_back(Variable("Heavy_ranged", "Hr", 0, 10+ph));
		variables.push_back(Variable("Ranged_ranged", "Rr", 0, 10+pr));

		variables.push_back(Variable("Plan_light", "pl", pl, 10+pl)); // 9
		variables.push_back(Variable("Plan_heavy", "ph", ph, 10+ph)); // 10
		variables.push_back(Variable("Plan_ranged", "pr", pr, 10+pr)); // 11

		vector<reference_wrapper<Variable>> variables_ref(variables.begin(), variables.end());

		shared_ptr<Constraint>ranged = make_shared<Leq>(variables_ref, vector<double>{0., 0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1.}, 0);
		shared_ptr<Constraint>heavy = make_shared<Leq>(variables_ref, vector<double>{0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0.}, 0);
		shared_ptr<Constraint>light = make_shared<Leq>(variables_ref, vector<double>{1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0., 0.}, 0);
		shared_ptr<Constraint>cst_ressources = make_shared<Leq_param>(variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 0., 0., 2., 3., 2.}, resources, pl, ph, pr);

		vector< shared_ptr<Constraint> > constraints = {ranged, heavy, light, cst_ressources};

		/*
		 * identity => RDU becomes Expected Utility
		 * logistic => pessimistic behavior
		 * logit / flat (but prefer logit) => optimistic behavior
		 * inverse_logistic => insane optimistic behavior
		 */
		//auto phi_callback = identity();
		//auto phi_callback = pessimistic();
		auto phi_callback = optimistic();
		//auto phi_callback = flat();
		//auto phi_callback = logistic( LAMBDA );
		//auto phi_callback = inverse_logistic( LAMBDA );
		//auto phi_callback = logit( LAMBDA );

		// cout << "phi(0.1) = " << phi_callback(0.1)
		//      << ", phi(0.5) = " << phi_callback(0.5)
		//      << ", phi(0.9) = " << phi_callback(0.9) << "\n";

		// One row unit can handle x column units
		/* /    H      R      L
		 * H    1    0.472  2.675
		 * R  2.119    1    0.639
		 * L  0.374  1.564    1
		 */

		int need_H = enemy_number_light - ph;
		int need_R = enemy_number_heavy - pr;
		int need_L = enemy_number_ranged - pl;

		array<bool, 3> to_produce; // H, R, L
		to_produce.fill( false );

		if( need_H > need_R )
		{
			if( need_H > need_L )
				// cout << "Should produce H\n";
				to_produce[0] = true;
			else
			{
				if( need_H == need_L )
				{
					// cout << "Should produce either H or L\n";
					to_produce[0] = true;
					to_produce[2] = true;
				}
				else
					// cout << "Should produce L\n";
					to_produce[2] = true;
			}
		}
		else
		{
			if( need_H == need_R )
			{
				if( need_L > need_R )
					// cout << "Should produce L\n";
					to_produce[2] = true;
				else
				{
					if( need_L == need_R )
					{
						// cout << "Should produce either H, R or L\n";
						to_produce[0] = true;
						to_produce[1] = true;
						to_produce[2] = true;
					}
					else
					{
						// cout << "Should produce either H or R\n";
						to_produce[0] = true;
						to_produce[1] = true;
					}
				}
			}
			else
			{
				if( need_L > need_R )
					// cout << "Should produce L\n";
					to_produce[2] = true;
				else
				{
					if( need_L == need_R )
					{
						// cout << "Should produce either R or L\n";
						to_produce[1] = true;
						to_produce[2] = true;
					}
					else
						// cout << "Should produce R\n";
						to_produce[1] = true;
				}
			}
		}

		// cout << to_produce[0] << " " << to_produce[1] << " " << to_produce[2] << "\n";

		// cout << "H beats L, L beats R, R beats H\n"
		//      << "Time: " << time << "\n\n"
		//      << "/////////////////////\nIdentity\n";

		shared_ptr<Objective> obj = make_shared<MaxDiff>( vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.},
																											samples,
																											phi_callback );

		Solver solver_identity( variables, constraints, obj );

		vector<int>solution(variables.size(), 0);
		double cost = 0.;

		solver_identity.solve( cost, solution, 10000, 100000 );

		if( solution[10] - ph == 1 && to_produce[0] )
			//cout << "1\n";
			++success_identity;
		else
			if( solution[11] - pr == 1 && to_produce[1] )
				//cout << "1\n";
				++success_identity;
			else
				if( solution[9] - pl == 1 && to_produce[2] )
					//cout << "1\n";
					++success_identity;
		//else
		//cout << "0\n";

		// cout << solver_identity.solve( cost, solution, 10000, 100000 ) << " : " << cost << " / " << obj->cost(variables) << "\n";
		// cout << "Total enemies: H" << enemy_number_heavy << " R" << enemy_number_ranged << " L" << enemy_number_light << "\n";
		// cout << "Current units: R" << pr << " L" << pl << " H" << ph << "\n";
		// cout << "To produce: H" << solution[10]-ph << " R" << solution[11]-pr << " L" << solution[9]-pl <<"\n";

		// cout << "\n/////////////////////\nLogistic - pessimistic\n";

		// phi_callback = logistic( LAMBDA );
		// obj = make_shared<MaxDiff>( vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.},
		//				samples,
		//				phi_callback );

		// Solver solver_logistic( variables, constraints, obj );

		// std::fill( solution.begin(), solution.end(), 0 );
		// cost = 0.;

		// solver_logistic.solve( cost, solution, 10000, 100000 );

		// if( solution[10] - ph == 1 && to_produce[0] )
		//   //cout << "1\n";
		//   ++success_logistic;
		// else
		//   if( solution[11] - pr == 1 && to_produce[1] )
		//	//cout << "1\n";
		//	++success_logistic;
		//   else
		//	if( solution[9] - pl == 1 && to_produce[2] )
		//		//cout << "1\n";
		//		++success_logistic;
		// //else
		// //cout << "0\n";

		// // cout << solver_logistic.solve( cost, solution, 10000, 100000 ) << " : " << cost << " / " << obj->cost(variables) << "\n";
		// // cout << "Total enemies: H" << enemy_number_heavy << " R" << enemy_number_ranged << " L" << enemy_number_light << "\n";
		// // cout << "Current units: R" << pr << " L" << pl << " H" << ph << "\n";
		// // cout << "To produce: H" << solution[10]-ph << " R" << solution[11]-pr << " L" << solution[9]-pl <<"\n";

		// // cout << "\n/////////////////////\nLogit - optimistic\n";

		// phi_callback = logit( LAMBDA );
		// obj = make_shared<MaxDiff>( vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.},
		//				samples,
		//				phi_callback );

		// Solver solver_logit( variables, constraints, obj );

		// std::fill( solution.begin(), solution.end(), 0 );
		// cost = 0.;

		// solver_logit.solve( cost, solution, 10000, 100000 );

		// if( solution[10] - ph == 1 && to_produce[0] )
		//   //cout << "1\n";
		//   ++success_logit;
		// else
		//   if( solution[11] - pr == 1 && to_produce[1] )
		//	//cout << "1\n";
		//   ++success_logit;
		//   else
		//	if( solution[9] - pl == 1 && to_produce[2] )
		//		//cout << "1\n";
		//		++success_logit;
		// //else
		// //cout << "0\n";

		// // cout << solver_logit.solve( cost, solution, 10000, 100000 ) << " : " << cost << " / " << obj->cost(variables) << "\n";
		// // cout << "Total enemies: H" << enemy_number_heavy << " R" << enemy_number_ranged << " L" << enemy_number_light << "\n";
		// // cout << "Current units: R" << pr << " L" << pl << " H" << ph << "\n";
		// // cout << "To produce: H" << solution[10]-ph << " R" << solution[11]-pr << " L" << solution[9]-pl <<"\n";

		// // cout << "\n/////////////////////\nInverse Logistic - crazy optimistic\n";

		// phi_callback = logit( LAMBDA );
		// obj = make_shared<MaxDiff>( vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.},
		//				samples,
		//				phi_callback );

		// Solver solver_inverse_logistic( variables, constraints, obj );

		// std::fill( solution.begin(), solution.end(), 0 );
		// cost = 0.;

		// solver_inverse_logistic.solve( cost, solution, 10000, 100000 );

		// if( solution[10] - ph == 1 && to_produce[0] )
		//   //cout << "1\n";
		//   ++success_inverse_logistic;
		// else
		//   if( solution[11] - pr == 1 && to_produce[1] )
		//	//cout << "1\n";
		//	++success_inverse_logistic;
		//   else
		//	if( solution[9] - pl == 1 && to_produce[2] )
		//		//cout << "1\n";
		//		++success_inverse_logistic;
		// //else
		// //cout << "0\n";

		// // cout << solver_inverse_logistic.solve( cost, solution, 10000, 100000 ) << " : " << cost << " / " << obj->cost(variables) << "\n";
		// // cout << "Total enemies: H" << enemy_number_heavy << " R" << enemy_number_ranged << " L" << enemy_number_light << "\n";
		// // cout << "Current units: R" << pr << " L" << pl << " H" << ph << "\n";
		// // cout << "To produce: H" << solution[10]-ph << " R" << solution[11]-pr << " L" << solution[9]-pl <<"\n";

	} // loop

	cout << success_identity << "\n";
	// << success_logistic << "\n"
	// << success_logit << "\n"
	// << success_inverse_logistic << "\n";

	return EXIT_SUCCESS;
}
