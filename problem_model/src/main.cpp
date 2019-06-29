#include <unistd.h>
#include <ios>
#include <iostream>
#include <fstream>
#include <string>
#include <memory>

#include <vector>
#include <numeric>
//#include <chrono>

#include "ghost/variable.hpp"
#include "ghost/solver.hpp"

#include "constraints_rts.hpp"
#include "obj_rts.hpp"
#include "phi_function.hpp"

using namespace std;

int main(int argv, char *argc[])
{
	std::ifstream infile(argc[1]);

	int nb_samples = std::stoi(argc[2]);
	int sampled_worker, sampled_heavy, sampled_range, sampled_light;
	int resources, my_heavy_units, my_range_units, my_light_units;

	std::vector<std::vector<int>> samples;

	for(int i = 0; i < nb_samples; ++i)
	{
		infile >> sampled_worker >> sampled_heavy >> sampled_range >> sampled_light;
		std::vector<int> tmp { sampled_worker, sampled_heavy, sampled_range, sampled_light };
		samples.push_back( tmp );
	}
	
	infile >> my_heavy_units;
	infile >> my_range_units;
	infile >> my_light_units;
	infile >> resources;

	vector<Variable> variables;

	variables.push_back( Variable( "Light_heavy", "Lh", 0, 10 + my_light_units ) );
	variables.push_back( Variable( "Heavy_heavy", "Hh", 0, 10 + my_heavy_units ) );
	variables.push_back( Variable( "Ranged_heavy", "Rh", 0, 10 + my_range_units ) );

	variables.push_back( Variable( "Light_light", "Ll", 0, 10 + my_light_units ) );
	variables.push_back( Variable( "Heavy_light", "Hl", 0, 10 + my_heavy_units ) );
	variables.push_back( Variable( "Ranged_light", "Rl", 0, 10 + my_range_units ) );

	variables.push_back( Variable( "Light_ranged", "Lr", 0, 10 + my_light_units ) );
	variables.push_back( Variable( "Heavy_ranged", "Hr", 0, 10 + my_heavy_units ) );
	variables.push_back( Variable( "Ranged_ranged", "Rr", 0, 10 + my_range_units ) );

	variables.push_back( Variable("Plan_light", "pl", my_light_units, 10 + my_light_units ) ); // 9
	variables.push_back( Variable("Plan_heavy", "ph", my_heavy_units, 10 + my_heavy_units ) ); // 10
	variables.push_back( Variable("Plan_ranged", "pr", my_range_units, 10 + my_range_units ) ); // 11

	vector<reference_wrapper<Variable>> variables_ref( variables.begin(), variables.end() );

	shared_ptr<Constraint>ranged = make_shared<Leq>( variables_ref, vector<double>{0., 0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1.}, 0 );
	shared_ptr<Constraint>heavy = make_shared<Leq>( variables_ref, vector<double>{0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0.}, 0 );
	shared_ptr<Constraint>light = make_shared<Leq>( variables_ref, vector<double>{1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0., 0.}, 0 );
	shared_ptr<Constraint>cst_resources = make_shared<Leq_param>( variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 0., 0., 2., 3., 2.}, resources, my_light_units, my_heavy_units, my_range_units );

	vector< shared_ptr<Constraint> > constraints = {ranged, heavy, light, cst_resources};

#if defined(PESSIMISTIC) 
	auto phi_callback = pessimistic();
#elif defined(OPTIMISTIC)
	auto phi_callback = optimistic();
#else
	auto phi_callback = identity();
#endif
	
	shared_ptr<Objective> obj = make_shared<MaxDiff>( vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.},
																										samples,
																										phi_callback );

	Solver solver_p( variables, constraints, obj );

	vector<int>solution( variables.size(), 0 );
	double cost_p = 0.;

	/*
	 * POAdaptive is waiting for 6 lines
	 * The 3 first lines are dummy lines, just for debug
	 * The 3 last lines contain necessary information: number of heavy/range/light units to produce
	 */	
	cout << "Solve ..." << "\n";
	cout << solver_p.solve( cost_p, solution, 10000, 100000 ) << " : " << cost_p << " / " << obj->cost( variables ) << "\n";
	cout << solution[10] << solution[11]  << solution[9] << "\n";
	cout << solution[10] - my_heavy_units << "\n" << solution[11] - my_range_units << "\n" << solution[9] - my_light_units <<"\n";

	return 0;
}
